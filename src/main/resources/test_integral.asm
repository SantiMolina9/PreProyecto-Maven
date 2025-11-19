main:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    mov eax, 100
    mov [rbp-4], eax
    mov eax, 50
    mov [rbp-8], eax
    mov eax, 25
    mov [rbp-12], eax
    mov eax, 0
    mov [rbp-16], eax
    mov eax, 1
    mov [rbp-17], eax
    mov eax, 0
    mov [rbp-18], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-12]
    pop rbx
    sub ebx, eax
    mov eax, ebx
    mov [rbp-16], eax
    mov eax, [rbp-16]
    push rax
    mov eax, 2
    pop rbx
    imul ebx, eax
    mov eax, ebx
    mov [rbp-16], eax
    mov eax, [rbp-16]
    push rax
    mov eax, [rbp-12]
    pop rbx
    xchg eax, ebx
    cdq
    idiv ebx
    mov [rbp-16], eax
    mov eax, [rbp-17]
    push rax
    mov eax, [rbp-18]
    pop rbx
    and eax, ebx
    mov [rbp-19], eax
    mov eax, [rbp-17]
    push rax
    mov eax, [rbp-18]
    pop rbx
    or eax, ebx
    mov [rbp-20], eax
    mov eax, [rbp-18]
    xor eax, 1
    mov [rbp-21], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je endif_1
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-12]
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je endif_3
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-12]
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-16], eax
endif_3:
endif_1:
    mov eax, 0
    mov [rbp-25], eax
while_start_4:
    mov eax, [rbp-25]
    push rax
    mov eax, 10
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    cmp eax, 0
    je while_end_5
    mov eax, [rbp-25]
    push rax
    mov eax, 5
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    cmp eax, 0
    je endif_7
    mov eax, 0
    mov [rbp-17], eax
endif_7:
    mov eax, [rbp-25]
    push rax
    mov eax, 1
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-25], eax
    jmp while_start_4
while_end_5:
    mov eax, [rbp-4]
    push rax
    mov eax, 50
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, 100
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    pop rbx
    and eax, ebx
    push rax
    mov eax, [rbp-17]
    pop rbx
    or eax, ebx
    cmp eax, 0
    je endif_9
    mov eax, [rbp-21]
    push rax
    mov eax, [rbp-25]
    push rax
    mov eax, 10
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    pop rbx
    and eax, ebx
    cmp eax, 0
    je else_10
    mov eax, [rbp-16]
    push rax
    mov eax, 100
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-16], eax
    jmp endif_11
else_10:
    mov eax, [rbp-16]
    push rax
    mov eax, 50
    pop rbx
    sub ebx, eax
    mov eax, ebx
    mov [rbp-16], eax
endif_11:
endif_9:
    mov eax, 0
    mov [rbp-29], eax
    mov eax, 100
    mov [rbp-33], eax
while_start_12:
    mov eax, [rbp-29]
    push rax
    mov eax, [rbp-33]
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    push rax
    mov eax, [rbp-17]
    pop rbx
    and eax, ebx
    cmp eax, 0
    je while_end_13
    mov eax, [rbp-29]
    push rax
    mov eax, 1
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-29], eax
    mov eax, [rbp-33]
    push rax
    mov eax, 1
    pop rbx
    sub ebx, eax
    mov eax, ebx
    mov [rbp-33], eax
    mov eax, [rbp-29]
    push rax
    mov eax, [rbp-33]
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    cmp eax, 0
    je endif_15
    mov eax, 0
    mov [rbp-17], eax
endif_15:
    jmp while_start_12
while_end_13:
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    imul ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-12]
    pop rbx
    xchg eax, ebx
    cdq
    idiv ebx
    push rax
    mov eax, [rbp-25]
    pop rbx
    add ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-29]
    pop rbx
    sub ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-33]
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-37], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-12]
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    pop rbx
    and eax, ebx
    push rax
    mov eax, [rbp-37]
    push rax
    mov eax, 0
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    pop rbx
    or eax, ebx
    mov [rbp-38], eax
    mov eax, [rbp-37]
    jmp main_exit
main_exit:
    add rsp, 48
    pop rbp
    ret

