#include "server.h"

static DWORD WINAPI serverThread(void *threadParams)
{
    start_server();
    return 0;
}

int main()
{
    message[0] = '\0'; // Mensaje que se envia a todos los clientes
    DWORD threadDescriptor;
    CreateThread(NULL, 0, serverThread, NULL, 0, &threadDescriptor); // Hilo para el servidor

    while (running)
    {
        printf("Enviar: ");
        scanf_s("%s", message, BUFLEN);
        if (strcmp("close", message) == 0)
        {
            running = 0;
        }
        fflush(stdout);
    }
    printf("Cerrando el servidor...\n");
    Sleep(3000);

    return 0;
}
