
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
    sub rsp, 64
    mov eax, 10
    mov [rbp-4], eax
    mov eax, 20
    mov [rbp-8], eax
    mov dword [rbp-12], 0
    mov eax, 1
    mov [rbp-13], eax
    mov eax, 0
    mov [rbp-14], eax
    mov dword [rbp-15], 0
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-12], eax
    mov eax, [rbp-13]
    push rax
    mov eax, [rbp-14]
    pop rbx
    or eax, ebx
    mov [rbp-15], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-19], eax
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-4]
    pop rbx
    sub ebx, eax
    mov eax, ebx
    mov [rbp-23], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    imul ebx, eax
    mov eax, ebx
    mov [rbp-27], eax
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-4]
    pop rbx
    xchg eax, ebx
    cdq
    idiv ebx
    mov [rbp-31], eax
    mov eax, [rbp-13]
    push rax
    mov eax, [rbp-14]
    pop rbx
    and eax, ebx
    mov [rbp-32], eax
    mov eax, [rbp-13]
    push rax
    mov eax, [rbp-14]
    pop rbx
    or eax, ebx
    mov [rbp-33], eax
    mov eax, [rbp-13]
    xor eax, 1
    mov [rbp-34], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    mov [rbp-35], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    mov [rbp-36], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    mov [rbp-37], eax
    mov eax, [rbp-4]
    push rax
    mov eax, 5
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je endif_1
    mov eax, 100
    mov [rbp-12], eax
endif_1:
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-4]
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je else_2
    mov eax, 1
    mov [rbp-15], eax
    jmp endif_3
else_2:
    mov eax, 0
    mov [rbp-15], eax
endif_3:
    mov eax, [rbp-4]
    push rax
    mov eax, 0
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je else_4
    mov eax, [rbp-8]
    push rax
    mov eax, 0
    pop rbx
    cmp ebx, eax
    setg al
    movzx eax, al
    cmp eax, 0
    je else_6
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-12], eax
    jmp endif_7
else_6:
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    pop rbx
    sub ebx, eax
    mov eax, ebx
    mov [rbp-12], eax
endif_7:
    jmp endif_5
else_4:
    mov eax, 0
    mov [rbp-12], eax
endif_5:
    mov eax, 0
    mov [rbp-41], eax
while_start_8:
    mov eax, [rbp-41]
    push rax
    mov eax, 10
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    cmp eax, 0
    je while_end_9
    mov eax, [rbp-41]
    push rax
    mov eax, 1
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-41], eax
    jmp while_start_8
while_end_9:
    mov eax, 0
    mov [rbp-45], eax
    mov eax, 10
    mov [rbp-49], eax
while_start_10:
    mov eax, [rbp-45]
    push rax
    mov eax, [rbp-49]
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    push rax
    mov eax, [rbp-13]
    pop rbx
    and eax, ebx
    cmp eax, 0
    je while_end_11
    mov eax, [rbp-45]
    push rax
    mov eax, 1
    pop rbx
    add ebx, eax
    mov eax, ebx
    mov [rbp-45], eax
    mov eax, [rbp-49]
    push rax
    mov eax, 1
    pop rbx
    sub ebx, eax
    mov eax, ebx
    mov [rbp-49], eax
    jmp while_start_10
while_end_11:
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
    mov [rbp-53], eax
    mov eax, [rbp-4]
    push rax
    mov eax, [rbp-8]
    push rax
    mov eax, [rbp-12]
    pop rbx
    add ebx, eax
    mov eax, ebx
    pop rbx
    imul ebx, eax
    mov eax, ebx
    mov [rbp-57], eax
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
    mov eax, 100
    pop rbx
    cmp ebx, eax
    setl al
    movzx eax, al
    pop rbx
    and eax, ebx
    push rax
    mov eax, [rbp-12]
    push rax
    mov eax, 50
    pop rbx
    cmp ebx, eax
    sete al
    movzx eax, al
    pop rbx
    or eax, ebx
    mov [rbp-58], eax
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
    jmp main_exit
main_exit:
    add rsp, 64
    pop rbp
    ret

