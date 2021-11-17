#include "constants.h"

typedef struct player
{
    char type; // Si es jugador: '1' o '2', si es observer '3' o '4'
    int x, y;  // Posicion en el mapa
    int points;
    int lives;
    int socket_pos;
} player_t;

typedef struct enemy
{
    char name[6];
    int x, y;
    int speed;
    player_t *player;
} enemy_t;

typedef struct fruit
{
    char name[6];
    int x, y;
    int speed;
    player_t *player;
} fruit_t;

int map_positions[4][4] = {{1, 1, 1, 1},
                           {1, 1, 1, 1},
                           {0, 0, 0, 0},
                           {1, 0, 1, 0}};

player_t players[4];
enemy_t *enemys;

void update_player(char type, int x, int y, int points, int lives)
{
    for (int i = 0; i < 4; i++)
    {
        if (players[i].type == type)
        {
            players[i].x = x;
            players[i].y = y;
            players[i].points = points;
            players[i].lives = lives;
            break;
        }
    }
}

void process_message(char *message)
{
}