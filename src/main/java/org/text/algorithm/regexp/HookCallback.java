package org.text.algorithm.regexp;

public interface HookCallback {
    /**
     * hook 发生时的回调
     * hook 是指正则中的 \h{XXX,YYY} 其中 XXX YYY 是两个整数，用来在回调时区分 hook
     *
     * @param type  hook类型 ： 0 DFA ， 1 单条正则（从而是NFA）
     * @param dfaId 当 type=1 代表单个正则的id
     * @param args  : 目前四个整数
     *              第一个代表定义hook时 (\h) 指定的第一个整数
     *              第二个代表定义hook时（\h) 指定的第二个整数
     *              例如 \h{11,50}  表示 第一个数 是11 ， 第二个数 是 50
     *              第三个数是hook 开始时的位置
     *              第四个参数是hook 发生时的当前位置
     * @return <0 或 >2  表示 hook 失败，停止hook
     * 0  继续hook
     * 1 hook成功完成，不需要继续hook
     * 2 hook已经成功， 但还是要继续hook
     * 只有hook成功后后面的字符才可以继续匹配
     */
    int hook(int type, int dfaId, int[] args);
}
