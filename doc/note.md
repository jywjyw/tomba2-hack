FF00 换行
FF11 红色
FF10 正常色
FF02 空格
FF01 enter
FF07 三角
□※○


脚本结构：
个数 + N个指针 + FFFF + N段文本
2Bytes - unknown
2Bytes - pointer个数+1
12Bytes - 0

2Bytes - 某个文本段的相对起始位置
2Bytes - unknown
最后跟上FFFF代表结束
最后再补齐FF至16字节的倍数

