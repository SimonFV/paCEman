#include "constants.h"

char running = !0; // estado del servidir, mantiene vivo el loop principal
char message[BUFLEN];

HANDLE msg_in_mutex, msg_out_mutex; // Mutex par accesar a los mensajes desde diferentes hilos

// Estructura que almacena los mensajes y quien los envia o recibe
typedef struct message
{
    char text[BUFLEN];
    int current_client;
} message_t;

message_t msg_in, msg_out; // Mensajes de entrada y salida, respectivamente

// Funciones que actualizan los mensajes enviados y recibidos utilizando mutex
// Parametros: Mensaje, Index del cliente (-1 para enviar a todos)
void update_msg_in(char *new_msg, int client)
{
    WaitForSingleObject(msg_in_mutex, INFINITE);
    strcpy_s(msg_in.text, BUFLEN, new_msg);
    msg_in.current_client = client;
    ReleaseMutex(msg_in_mutex);
}
void update_msg_out(char *new_msg, int client)
{
    WaitForSingleObject(msg_out_mutex, INFINITE);
    strcpy_s(msg_out.text, BUFLEN, new_msg);
    msg_out.current_client = client;
    ReleaseMutex(msg_out_mutex);
}

// Funcion que crea y ejecuta el servidor.
// Retorna 0 si no ocurren errores durante el proceso, 1 en caso contrario.
int start_server()
{
    printf("Iniciando servidor...\n");

    int res, sendRes; // Resultados de algunas operaciones

    // INICIALIZACION DE WINSOCK ===========================
    WSADATA wsaData; // informacion de la configuracion
    res = WSAStartup(MAKEWORD(2, 2), &wsaData);
    if (res)
    {
        printf("Inicializacion de Winsock fallida: %d\n", res);
        return 1;
    }
    // ==========================================

    // CONFIGURACION DEL SERVIDOR =============================

    // Construccion del socket que espera nuevas conexiones
    SOCKET listener;
    listener = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (listener == INVALID_SOCKET)
    {
        printf("Error al construir el socket: %d\n", WSAGetLastError());
        WSACleanup();
        return 1;
    }

    // Configuracion para conexiones multiples
    char multiple = !0;
    res = setsockopt(listener, SOL_SOCKET, SO_REUSEADDR, &multiple, sizeof(multiple));
    if (res < 0)
    {
        printf("Multiple client setup failed: %d\n", WSAGetLastError());
        closesocket(listener);
        WSACleanup();
        return 1;
    }

    // Proceso de Binding a una direccion
    struct sockaddr_in address;
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = inet_addr(ADDRESS);
    address.sin_port = htons(PORT);
    res = bind(listener, (struct sockaddr *)&address, sizeof(address));
    if (res == SOCKET_ERROR)
    {
        printf("Bind fallido: %d\n", WSAGetLastError());
        closesocket(listener);
        WSACleanup();
        return 1;
    }

    // Configura el socket listener
    res = listen(listener, SOMAXCONN);
    if (res == SOCKET_ERROR)
    {
        printf("Configuracion del socket listener fallida: %d\n", WSAGetLastError());
        closesocket(listener);
        WSACleanup();
        return 1;
    }

    // Configuracion exitosa
    printf("Configuracion finalizada. Aceptando conexiones en: %s:%d\n", ADDRESS, PORT);

    // ==========================================

    // LOOP PRINCIPAL ================================

    // Variables relacionadas a los clientes
    fd_set socketSet;              // set de clientes activos
    SOCKET clients[MAX_CLIENTS];   // lista de clientes
    int curNoClients = 0;          // slots activos en la lista de clientes
    SOCKET sd, max_sd;             // placeholders para los clientes
    struct sockaddr_in clientAddr; // direccion del cliente
    int clientAddrlen;             // tamaño de la direccion del cliente

    char recvbuf[BUFLEN]; // buffer para los mensajes recibidos

    // Algunos mensajes comunes enviados al cliente
    char *welcome = "Bienvenido al servidor!;";
    int welcomeLength = strlen(welcome);
    char *full = "Servidor lleno.;";
    int fullLength = strlen(full);
    char *goodbye = "Adios.;";
    int goodbyeLength = strlen(goodbye);

    // Limpia el array de clientes
    memset(clients, 0, MAX_CLIENTS * sizeof(SOCKET));

    while (running)
    {
        // Limpia el set
        FD_ZERO(&socketSet);

        // Añade el socket listener al set
        FD_SET(listener, &socketSet);
        max_sd = listener;

        for (int i = 0; i < MAX_CLIENTS; i++)
        {
            // socket
            sd = clients[i];

            if (sd > 0)
            {
                // Añade un cliente activo al set
                FD_SET(sd, &socketSet);
            }

            if (sd > max_sd)
            {
                max_sd = sd;
            }
        }

        // Espera por actividad en alguno de los sockets
        struct timeval tv = {0, 200000}; // Tiempo maximo que espera por actividad en segundos
        int activity = select(max_sd + 1, &socketSet, NULL, NULL, &tv);
        if (activity < 0)
        {
            continue;
        }

        // Envia el mensaje a los clientes
        WaitForSingleObject(msg_out_mutex, INFINITE);
        if (strlen(msg_out.text) > 0)
        {
            if (msg_out.current_client == -1) // Envia el mensaje a todos
            {
                for (int i = 0; i < MAX_CLIENTS; i++)
                {
                    if (!clients[i])
                    {
                        continue;
                    }

                    sd = clients[i];
                    sendRes = send(sd, msg_out.text, strlen(msg_out.text), 0);
                    if (sendRes == SOCKET_ERROR)
                    {
                        printf("Error al enviar mensaje devuelta: %d\n", WSAGetLastError());
                        shutdown(sd, SD_BOTH);
                        closesocket(sd);
                        clients[i] = 0;
                        curNoClients--;
                    }
                }
            }
            else //Envia el mensaje a un cliente especifico
            {
                if (clients[msg_out.current_client])
                {
                    sd = clients[msg_out.current_client];
                    sendRes = send(sd, msg_out.text, strlen(msg_out.text), 0);
                    if (sendRes == SOCKET_ERROR)
                    {
                        printf("Error al enviar mensaje devuelta: %d\n", WSAGetLastError());
                        shutdown(sd, SD_BOTH);
                        closesocket(sd);
                        clients[msg_out.current_client] = 0;
                        curNoClients--;
                    }
                }
            }
            msg_out.text[0] = '\0';
        }
        ReleaseMutex(msg_out_mutex);

        // Determina si el listener presenta actividad
        if (FD_ISSET(listener, &socketSet))
        {
            // Accepta la conexion
            sd = accept(listener, NULL, NULL);
            if (sd == INVALID_SOCKET)
            {
                printf("Error al aceptar el cliente: %d\n", WSAGetLastError());
            }

            // Obtiene la informacion del cliente
            getpeername(sd, (struct sockaddr *)&clientAddr, &clientAddrlen);
            printf("Cliente conectado en: %s:%d\n",
                   inet_ntoa(clientAddr.sin_addr), ntohs(clientAddr.sin_port));

            // Añade el nuevo cliente al array si hay espacio disponible
            if (curNoClients >= MAX_CLIENTS)
            {
                printf("Servidor lleno. Conexion rechazada.\n");

                // Envia un mensaje al cliente indicando que el servidor se encuentra lleno
                sendRes = send(sd, full, fullLength, 0);
                if (sendRes != fullLength)
                {
                    printf("Error al enviar mensaje: %d\n", WSAGetLastError());
                }

                shutdown(sd, SD_BOTH);
                closesocket(sd);
            }
            else
            {
                // Escanea la lista para añadir el cliente nuevo en un espacio desocupado
                int i;
                for (i = 0; i < MAX_CLIENTS; i++)
                {
                    if (!clients[i])
                    {
                        clients[i] = sd;
                        printf("Cliente agregado en la posicion: %d\n", i);
                        curNoClients++;
                        break;
                    }
                }

                // Envia un mensaje de bienvenida al cliente
                sendRes = send(sd, welcome, welcomeLength, 0);
                if (sendRes != welcomeLength)
                {
                    printf("Error al enviar mensaje: %d\n", WSAGetLastError());
                    shutdown(sd, SD_BOTH);
                    closesocket(sd);
                    clients[i] = 0;
                    curNoClients--;
                }
            }
        }

        // Itera la lista de clientes
        for (int i = 0; i < MAX_CLIENTS; i++)
        {
            if (!clients[i])
            {
                continue;
            }

            sd = clients[i];
            // Determina si el cliente presenta actividad
            if (FD_ISSET(sd, &socketSet))
            {
                // Recibe el mensaje
                res = recv(sd, recvbuf, BUFLEN, 0);
                if (res > 0)
                {
                    // Imprime el mensaje recibido
                    recvbuf[res] = '\0';
                    printf("Recibido (%d): %s\n", res, recvbuf);

                    // Revisa si el mensaje es para cerrar el servidor
                    if (!memcmp(recvbuf, "/quit", 5 * sizeof(char)))
                    {
                        running = 0; // false
                        break;
                    }

                    // Reenvia el mismo mensaje devuelta al cliente
                    /*
                    sendRes = send(sd, recvbuf, res, 0);
                    if (sendRes == SOCKET_ERROR)
                    {
                        printf("Error al enviar mensaje devuelta: %d\n", WSAGetLastError());
                        shutdown(sd, SD_BOTH);
                        closesocket(sd);
                        clients[i] = 0;
                        curNoClients--;
                    } */
                }
                else
                {
                    // Cliente desconectado
                    getpeername(sd, (struct sockaddr *)&clientAddr, &clientAddrlen);
                    printf("Cliente desconectado: %s:%d\n",
                           inet_ntoa(clientAddr.sin_addr), ntohs(clientAddr.sin_port));

                    shutdown(sd, SD_BOTH);
                    closesocket(sd);
                    clients[i] = 0;
                    curNoClients--;
                }
            }
        }
    }

    // ==========================================

    // LIMPIEZA ==================================

    // Desconecta todos los clientes
    for (int i = 0; i < MAX_CLIENTS; i++)
    {
        if (clients[i] > 0)
        {
            // Envia un mensaje a los clientes activos que el servidor se cerro
            sendRes = send(clients[i], goodbye, goodbyeLength, 0);

            shutdown(clients[i], SD_BOTH);
            closesocket(clients[i]);
            clients[i] = 0;
        }
    }

    // Cierra el socket del servidor
    closesocket(listener);

    // Limpia Winsock
    res = WSACleanup();
    if (res)
    {
        printf("Limpieza de Winsock fallida: %d\n", res);
        return 1;
    }

    printf("Servidor cerrado.\n");
    // ==========================================

    return 0;
}