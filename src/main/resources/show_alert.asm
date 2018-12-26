//限制43条指令
//in param. v1=char code(8xxx)
address=8004f534
lui v0,6480 //3行,write sprite
ori v0,v0,8080
sw v0,fff7(a1) 
srl t1,v1,8		//4行,save clut
andi t1,t1,f
addiu t1,t1,783c
sh t1,1(a1) //clut
sh a2,fffb(a1) //x
sh s3,fffd(a1) //y
andi t1,v1,1f  //6行,save u
sll t2,t1,3
sll t1,t1,1
add t1,t2,t1
andi t1,t1,ff
sb t1,ffff(a1) //u

andi t1,v1,ff //7行:save v
srl t2,t1,5  
sll t3,t2,3
sll t2,t2,1
add t1,t2,t3
andi t1,t1,ff
sb t1,0(a1) //v
addiu t1,r0,a
sh t1,3(a1) //w
addiu t1,r0,a
sh t1,5(a1) //h
lui v0,800f	//3行,load saved link head
lw a0,f570(v0)
lw t1,c(a0) 
lui t2,0400  //2行,拼接dma head
or t1,t1,t2
sw t1,0(s1) //write dma head
sw s1,c(a0) //update saved link head
addiu a1,a1,14 //dma size+=20
addiu a2,a2,a //x+=10
j 8004f5e0
addiu s1,s1,14 //next link head addr+=20