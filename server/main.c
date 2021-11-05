#include "server.h"

// Funcion que corre en un hilo aparte para ejecutar el servidor
static DWORD WINAPI serverThread(void *threadParams)
{
    start_server();
    return 0;
}

int main()
{
    message[0] = '\0'; // Inicializa el mensaje que se envia a todos los clientes
    char input[BUFLEN];
    DWORD threadDescriptor;
    CreateThread(NULL, 0, serverThread, NULL, 0, &threadDescriptor); // Hilo para el servidor

    while (running)
    {
        scanf_s("%s", input, BUFLEN);
        if (strcmp("close", input) == 0)
        {
            running = 0;
        }
        else
        {
            strcpy_s(message, BUFLEN, input);
        }
        fflush(stdout);
    }
    printf("Cerrando el servidor...\n");
    Sleep(1000);

    return 0;
}
