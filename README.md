# 基于pike regexp 的正则集合匹配

在很多应用进行文本匹配时，匹配条件是正则表达式。当有很多（几十万）个正则条件时，
不可能采用逐个匹配的方法。这里开发了一个正则库，采用将所有正则编译成一个巨大的DFA思路来完成匹配。
在转换过程中，常规的方法会遇到巨大的问题，导致无法生成DFA。这里做了大量优化工作，有时甚至牺牲了一点性能。

第一步，单个DFA采用 pike regexp， 编译成pike vm。这个步骤中正则是真实的，不会损失任何精度。
考虑到正则的复杂性，这里只能支持部分正则的特性（pike vm 的限制）。此外，我们也适当扩充了一些功能

第二步，将每个pike vm 转换成单个的DFA（其实是先转换成NFA，再转换为DFA），并极小化各DFA。
因为后续步骤要将各DFA 合并为一个大的DFA，这里做了一些优化：

+ 各种DFA不能支持的功能（括号捕获，环视，\b 等），都转换成了 epsilon 边，相当于忽略，这部分丢失了精度
+ 目的状态相同的数量巨大的边（主要是一些范围运算）替换为特殊的边（-1），表示任意字符，这丢失了精度
+ 任意字符不展开，记录为特殊的边（-1），没有丢失精度

第三步，用子集构造法将各DFA合并成大的DFA（相当于各DFA做union）。虽然有上述优化，DFA数量大或者复杂时，这任然可能无法进行。
我们设置了一个状态数上限，超过上限时，剩余的没有处理完成的DFA的剩余状态保留起来，将来匹配时如果到达这些状态，将进入该单DFA的状态进行匹配。

第四步， 目标DFA 不再极小化（这可能无法进行），但保留下面内容：
+ 转换完成的状态
+ 未转换完成的状态
+ 未转换完成的状态对应用到的各单DFA的状态
+ 各正则的pike vm

匹配时步骤如下：
1. 在总DFA 中进行匹配，得到的集合为候选正则的id列表
+ 每个状态正常边匹配
+ 每个状态检查有没有-1 边，视为任意字符都满足的匹配
+ 每个状态检查有没有我们扩展的边，做扩展匹配。目前的扩展边是hook边。

2. 遍历候选正则，用对应的pike vm 再做匹配，命中的才算最终命中结果。

## 附录 支持的正则语法
* 普通的 unicode 字符
* . ? * + ^ $ | ( ) {n,m} [abcd] [a-b] [^abcd] [^a-b] 等普通正则语法
*  \b  英语的单词边界
*  \B  英语的非单词边界
*  \p{xxx}  unicode 属性类别
*  \P{xxx}  unicode 属性类别取反
*  \a 0x0007;
*  \e 0x001B;
*  \f 0x000C;
*  \n 0x000A;
*  \r 0x000D;
*  \t 0x0009;
*  \d 数字
*  \D 非数字
*  \w 单词字符
*  \W 非单词字符   
*  \\\\  \\
*  \\.  .
*  \\* *
*  \\+ +
*  \\? ?
*  \\| |
*  \\[ [
*  \\] ]
* \\{ {
* \\} }
* \\( (
* \\) )
* \\^ ^
* \\$ $
* \xab  ascii 编码
* \uabcd unicode 编码
*  \i{start,end} 整数范围，start， end 是数字，表示范围的起始，都包括（相当于闭区间）
*  \h{pa1,pa2}  扩展功能，调用者hook， pa1  pa2 为数字，是 hook 的参数
*  (?:exp)  不捕获的括号
*  (?=exp) 向前看
*  (?!exp) 向前看取反
*  (?<=exp) 向后看
*  (?<!exp) 向后看取反
* <start_end> 相当于 \i{start, end}
* \<abc\>  引用一个名称为 abc的正则。当编译单个Pike vm时，需要提供一个ExpFactory，查询该正则的定义。编译正则集合时， 引用的正则必须出现在前面
  
    >      如果 abc = all, 代表引用所有名称不以 _ 开头的正则
    >      如果 abc = _all, 代表引用前面的所有正则