address=8004f410
lb a0,0(s1)  //410:s1为字符编码地址
sll a0,a0,8 //414
lb t1,1(s1) //418
or a0,a0,t1 //41c
andi a0,a0,ffff //420
ori v0,r0,ff02 //424
bne a0,v0,8004f434 //428
j 8004f458 //42c
sh a0,0(s0) //430
sh a0,0(s0) //434
nop
j 8004f45c
addiu s0,s0,2 //440