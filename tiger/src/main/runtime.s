_malloc:
    li $v0,9
    syscall
    j $ra

_printi:
    li $v0, 1
    syscall
    j $ra

_print:
    li $v0, 4
    syscall
    j $ra

_flush:
    j $ra

_strcmp:
    strcmp_test:
    lb $a2 ($a0)
    lb $a3 ($a1)
    beq $a2, $zero, strcmp_end
    beq $a3, $zero, strcmp_end
    bgt $a2, $a3  strcmp_great
    blt $a2, $a3  strcmp_less
    add $a0, $a0, 1
    add $a1, $a1, 1
    j strcmp_test
    strcmp_great:
    li $v0, 1
    j $ra
    strcmp_less:
    li $v0, -1
    j $ra
    strcmp_end:
    bne $a2 $zero strcmp_great
    bne $a3 $zero strcmp_less
    li $v0, 0
    j $ra

_size:
    move $v0, $zero
    size_loop:
    lb $a1 ($a0)
    beq $a1, $zero size_exit
    add $v0, $v0, 1
    add $a0, $a0, 1
    j size_loop
    size_exit:
    j $ra

_ord:
    lb $a1,($a0)
    li $v0,-1
    beqz $a1,Lrunt5
    lb $v0,($a0)
    Lrunt5:
    j $ra

_getchar:
    li $v0, 9 
    li $a0, 2
    syscall
    move $a0, $v0
    li $a1, 2
    li $v0, 8
    syscall
    move $v0, $a0
    j $ra

_chr:
    move $a1, $a0
    li $v0, 9
    li $a0, 2
    syscall
    sb $a1 ($v0)
    sb $zero 1($v0)
    j $ra

_exit:
    li $v0, 10
    syscall

_substring:
    add $a1, $a0, $a1
    move $a3, $a1
    li $v0, 9
    add $a2, $a2, 1
    move $a0, $a2
    add $a0, $a0, 1 
    syscall
    # got a new string in $v0
    add $a2,$a2,$a3
    add $a2,$a2,-1
    move $a0, $v0
    substring_copy:
    beq $a1 $a2 substring_exit
    lb $a3 ($a1)
    sb $a3 ($a0)
    add $a1, $a1, 1
    add $a0, $a0, 1 
    j substring_copy
    substring_exit:
    sb $zero, ($a0)
    j $ra

_not:
    seq $v0,$a0,0
    j $ra

_copy:
    copy_loop:
    lb $a2, ($a1)
    beq $zero, $a2 copy_exit 
    sb $a2, ($a0)   
    add $a0,$a0,1
    add $a1,$a1,1
    j copy_loop
    copy_exit:
    sb $zero, ($a0)
    move $v0, $a0
    j $ra

_concat:
    sw $a0, -4($sp)
    sw $a1, -8($sp)
    sw $ra, -12($sp)
    jal _size
    li $a3, 1
    add $a3,$a3,$v0
    lw $a0, -8($sp)
    jal _size
    add $a3, $a3, $v0
    move $a0, $a3
    li $v0, 9
    syscall 
    move $a3, $v0
    move $a0, $v0
    lw   $a1, -4($sp)
    jal _copy
    move $a0, $v0
    lw $a1, -8($sp)
    jal _copy
    move $v0, $a3
    lw $ra, -12($sp)
    j $ra
