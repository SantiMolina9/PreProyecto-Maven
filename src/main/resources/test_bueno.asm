main:
    push rbp
    mov rbp, rsp
    sub rsp, 16
    mov eax, 10
    mov [rbp-4], eax
    mov eax, 20
    mov [rbp-8], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-12], eax
    mov eax, [rbp-4]
    jmp main_exit
main_exit:
    add rsp, 16
    pop rbp
    ret

