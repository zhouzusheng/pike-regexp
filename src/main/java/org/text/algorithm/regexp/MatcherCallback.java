package org.text.algorithm.regexp;

public interface MatcherCallback {
    /**
     * 报告匹配到一个正则
     * 返回 true 才继续调用 hitInfo
     * 使用者可以用来做一些排除逻辑，比如某些正则不算
     * id 是用户自己编译正则是设置的正则id而不是内部编号
     */
    default boolean hit(int id) {
        return true;
    }

    /**
     * 一个正则匹配的详情
     * 注意：如果一个正则有多次命中， 这里会调用多次，每次传递命中的详情
     *
     * @param id    命中的正则 id
     * @param start 命中的开始位置， 下标为0 表示 整个范围， 否则是第 X 个括号的范围。
     *              对不需要捕获的括号(?:xx)， 值为 -1
     * @param end   命中的结束位置
     */
    void hitInfo(int id, int[] start, int[] end);

    /**
     * 对一个正则，所有命中结束后调用一次
     *
     * @param id
     */
    default void hitEnd(int id) {
    }
}
