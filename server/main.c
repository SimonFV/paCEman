#include "server.h"

// Funcion que corre en un hilo aparte para ejecutar el servidor
static DWORD WINAPI serverThread(void *threadParams)
{
    start_server();
    return 0;
}

int main()
{
    message[0] = '\0';      // Inicializa el mensaje que se envia a todos los clientes
    char key_input[BUFLEN]; // String para almacenar el input de teclado
    msg_in_mutex = CreateMutex(NULL, 0, NULL);
    msg_out_mutex = CreateMutex(NULL, 0, NULL);
    HANDLE server_thread = CreateThread(NULL, 0, serverThread, NULL, 0, NULL); // Hilo para el servidor

    while (running)
    {
        scanf_s("%s", key_input, BUFLEN);
        if (strcmp("close", key_input) == 0)
        {
            running = 0;
        }
        else
        {
            update_msg_out(key_input);
        }
        fflush(stdout);
    }

    printf("Cerrando el servidor...\n");
    Sleep(1000);
    CloseHandle(server_thread);

    return 0;
}
