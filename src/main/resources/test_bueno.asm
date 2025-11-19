
section .text
global _start

_start:
    call main
    mov ebx, eax    ; exit code
    mov eax, 1      ; sys_exit
    int 0x80

main:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov eax, 10
    mov [rbp-4], eax
    mov eax, 20
    mov [rbp-8], eax
    mov dword [rbp-9], 0
    mov dword [rbp-13], 0
    mov dword [rbp-17], 0
    mov dword [rbp-21], 0
    mov dword [rbp-25], 0
    mov eax, [rbp-4]
    push rax
    mov eax, 10
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je else_0
    mov eax, 20
    mov [rbp-8], eax
    jmp endif_1
else_0:
    mov eax, 30
    mov [rbp-8], eax
endif_1:
while_start_2:
    mov eax, [rbp-25]
    push rax
    mov eax, 100
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    cmp eax, 0
    je while_end_3
    mov eax, [rbp-25]
    push rax
    mov eax, 1
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-25], eax
    jmp while_start_2
while_end_3:
    mov eax, [rbp-13]
    push rax
    mov eax, [rbp-17]
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    push rax
    mov eax, [rbp-21]
    push rax
    mov eax, 5
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    pop rbx
    and eax, ebx
    cmp eax, 0
    je endif_5
    mov eax, 1
    mov [rbp-9], eax
endif_5:
    mov eax, [rbp-9]
    jmp main_exit
main_exit:
    add rsp, 32
    pop rbp
    ret

