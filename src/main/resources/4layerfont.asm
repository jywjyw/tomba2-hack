//jump from 8007943c, write to 800a6fbc, 限制345条CPU指令
//1. 80079304和800792f0 => j 80079358, 作用:有个轮循字符编码的UV坐标的功能,需要跳过
//2. 8007928c => addiu t1,v1,0, 把字符编码复制到t1
//3. 8007943c => j 800a6fbc, 作用:重写sprite指令的生成步骤
//5. 800a6fbc => 写入本ASM代码

lw a1,1324(s3) //int a1=writeAddr=&s3

//写sprite指令
lw a0,f570(s4) //3行:读取链表头变量, int v0=lastDmaAddr
nop
addu a0,a0,s1
lw v0,0(a0)
lui v1,0400 //3行:write DMA链表头,sprite占4个字
or v0,v0,v1 
sw v0,0(a1)
sw a1,0(a0) //更新链表头变量
addiu a1,a1,4 //writeAddr+=4
lui v1,6480 //4行,write sprite word1
ori v1,v1,8080
sw v1,0(a1)
addiu a1,a1,4
lw v0,8(s2) //3行:load XY and write
nop
sw v0,0(a1)
addiu a1,a1,4

srl v0,t1,8 //计算clut
andi v0,v0,f
sll t3,v0,4
addiu t3,t3,03c0  //t3=clutX
srl t3,t3,4
lh t2,e(s2) //load exist clut
nop
srl t2,t2,6
addi t2,t2,fe10 //old base y = -496
addiu t2,t2,01e0 //new base y = 480
sll t2,t2,6
or v0,t2,t3
sll v0,v0,10
andi v1,t1,ff //v1=低位字符编码
srl t2,v1,5  //7行:计算v,存入v0,注意v的起始值为0
sll t3,t2,3
sll t4,t2,1
add t3,t3,t4
andi t3,t3,ff
sll t3,t3,8
or v0,v0,t3
andi t2,v1,1f //6行:计算u,存入v0
sll t3,t2,3
sll t4,t2,1
add t3,t3,t4
andi t3,t3,ff
or v0,v0,t3
sw v0,0(a1) //2行,保存uv_clut(格式:clut_v_u)
addiu a1,a1,4

lui v0,a	//4行,write WH
addiu v0,v0,a
sw v0,0(a1) 
addiu a1,a1,4

//收尾
sw a1,1324(s3)  //把写入指针保存起来,以便下次取用
lhu v0,8(s2) 	//load X
nop
addiu v0,v0,a	//v0代表屏幕坐标x, 写完一个字符后, x+=10
sh v0,8(s2)		//保存x
j 800794b4
nop