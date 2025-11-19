main:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov eax, 5
    mov [rbp-4], eax
    mov eax, 10
    mov [rbp-8], eax
    mov eax, 15
    mov [rbp-12], eax
    mov eax, 1
    mov [rbp-13], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-12]
    pop rbx
    imul ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-4]
    pop rbx
    sub ebx, eax
    mov eax, ebx
    pop rbx
    xchg eax, ebx
    cdq
    idiv ebx
    mov [rbp-17], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    imul ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-12]
    push rax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    sub ebx, eax
    mov eax, ebx
    pop rbx
    imul ebx, eax
    mov eax, ebx
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-21], eax
    mov eax, [rbp-4]
    push rax
    mov eax, 0
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, 20
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    pop rbx
    and eax, ebx
    push rax
    mov eax, [rbp-12]
    push rax
    mov eax, 15
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    pop rbx
    and eax, ebx
    mov [rbp-22], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-12]
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    pop rbx
    or eax, ebx
    push rax
    mov eax, [rbp-13]
    pop rbx
    or eax, ebx
    mov [rbp-23], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    xor eax, 1
    push rax
    mov eax, [rbp-12]
    push rax
    mov eax, 100
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    pop rbx
    and eax, ebx
    mov [rbp-24], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-12]
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    pop rbx
    and eax, ebx
    push rax
    mov eax, [rbp-13]
    pop rbx
    or eax, ebx
    mov [rbp-25], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-12]
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    push rax
    mov eax, [rbp-13]
    pop rbx
    and eax, ebx
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, 2
    pop rbx
    imul ebx, eax
    mov eax, ebx
    push rax
    mov eax, [rbp-12]
    push rax
    mov eax, [rbp-4]
    pop rbx
    add ebx, eax
    mov eax, ebx
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    pop rbx
    or eax, ebx
    mov [rbp-26], eax
    mov eax, [rbp-4]
    push rax
    mov eax, 0
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, 0
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    pop rbx
    and eax, ebx
    push rax
    mov eax, [rbp-12]
    push rax
    mov eax, 100
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    push rax
    mov eax, [rbp-13]
    pop rbx
    or eax, ebx
    pop rbx
    and eax, ebx
    cmp eax, 0
    je endif_1
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
    mov [rbp-17], eax
endif_1:
while_start_2:
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-12]
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    pop rbx
    and eax, ebx
    push rax
    mov eax, [rbp-13]
    pop rbx
    and eax, ebx
    cmp eax, 0
    je while_end_3
    mov eax, [rbp-4]
    push rax
    mov eax, 1
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-4], eax
    mov eax, [rbp-4]
    push rax
    mov eax, 10
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je endif_5
    mov eax, 0
    mov [rbp-13], eax
endif_5:
    jmp while_start_2
while_end_3:
    mov eax, [rbp-17]
    jmp main_exit
main_exit:
    add rsp, 32
    pop rbp
    ret

