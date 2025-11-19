main:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov dword [rbp-4], 0
    mov eax, 10
    mov [rbp-8], eax
    mov dword [rbp-12], 0
    mov dword [rbp-13], 0
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-17], eax
    mov eax, [rbp-12]
    push rax
    mov eax, 5
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je endif_1
    mov eax, 20
    mov [rbp-8], eax
endif_1:
    mov eax, [rbp-13]
    push rax
    mov eax, 1
    pop rbx
    and eax, ebx
    mov [rbp-18], eax
    mov eax, 5
    mov [rbp-4], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-17], eax
    mov eax, [rbp-17]
    jmp main_exit
main_exit:
    add rsp, 32
    pop rbp
    ret

