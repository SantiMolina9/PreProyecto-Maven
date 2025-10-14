section .data
    newline db 10, 0
    int_format db "%d", 0
    bool_true db "true", 0
    bool_false db "false", 0

section .text
global _start

_start:
    call main
    mov ebx, eax    ; exit code
    mov eax, 1      ; sys_exit
    int 0x80

    ; Inicio del programa
    ; Definición de función: main
main:
    push rbp
    mov rbp, rsp
    sub rsp, 16
    ; Inicializar variable: x
    ; Cargar número: 10
    mov eax, 10
    mov [rbp-4], eax
    ; Return statement
    ; Cargar variable: x
    mov eax, [rbp-4]
    jmp main_exit
main_exit:
    add rsp, 16
    pop rbp
    ret

