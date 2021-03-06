#include "game.h"

char running = !0; // estado del servidir, mantiene vivo el loop principal
char message[BUFLEN];

HANDLE msg_in_mutex, msg_out_mutex; // Mutex par accesar a los mensajes desde diferentes hilos

// Estructura que almacena los mensajes y quien los envia o recibe
char msg_out[BUFLEN], msg_in[BUFLEN]; // Mensajes de entrada y salida, respectivamente

// Funciones que actualizan los mensajes enviados y recibidos utilizando mutex
// Parametros: Mensaje, Index del cliente (-1 para enviar a todos)
void update_msg_in(char *new_msg)
{
    WaitForSingleObject(msg_in_mutex, INFINITE);
    strcpy_s(msg_in, BUFLEN, new_msg);
    ReleaseMutex(msg_in_mutex);
}
void update_msg_out(char *new_msg)
{
    WaitForSingleObject(msg_out_mutex, INFINITE);
    strcpy_s(msg_out, BUFLEN, new_msg);
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
    int clientAddrlen;             // tama??o de la direccion del cliente

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

        // A??ade el socket listener al set
        FD_SET(listener, &socketSet);
        max_sd = listener;

        for (int i = 0; i < MAX_CLIENTS; i++)
        {
            // socket
            sd = clients[i];

            if (sd > 0)
            {
                // A??ade un cliente activo al set
                FD_SET(sd, &socketSet);
            }

            if (sd > max_sd)
            {
                max_sd = sd;
            }
        }

        // ENVIO DE MENSAJES A TODOS LOS CLIENTES
        WaitForSingleObject(msg_out_mutex, INFINITE); //Bloquea el mensaje mientras es enviado
        for (int i = 0; i < MAX_CLIENTS; i++)
        {
            if (!clients[i])
            {
                continue;
            }
            sd = clients[i];

            // Envia el mensaje que esta en cola
            if (msg_out[0] != '\0')
            {
                sendRes = send(sd, msg_out, strlen(msg_out), 0);
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
        msg_out[0] = '\0';
        ReleaseMutex(msg_out_mutex);

        // Espera por actividad en alguno de los sockets
        struct timeval tv = {0, 200000}; // Tiempo maximo que espera por actividad en microsegundos
        int activity = select(max_sd + 1, &socketSet, NULL, NULL, &tv);
        if (activity < 0)
        {
            continue;
        }

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

            // A??ade el nuevo cliente al array si hay espacio disponible
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
                // Escanea la lista para a??adir el cliente nuevo en un espacio desocupado
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
                    recvbuf[res] = '\0'; // Agrega un limite al mensaje
                    printf("Recibido (%d): %s\n", res, recvbuf);

                    // PROCESA EL MENSAJE RECIBIDO POR EL CLIENTE
                    // ENVIA LA POSICION DE LOS OBJETOS A TODOS LOS OBSERVERS CONECTADOS
                    char msg_position[] = "pos X Y";
                    sendRes = send(sd, msg_position, strlen(msg_position), 0);
                    if (sendRes == SOCKET_ERROR)
                    {
                        printf("Error al enviar mensaje devuelta: %d\n", WSAGetLastError());
                        shutdown(sd, SD_BOTH);
                        closesocket(sd);
                        clients[i] = 0;
                        curNoClients--;
                        continue;
                    }
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