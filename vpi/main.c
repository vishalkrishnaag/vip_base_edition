#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

enum CellType
{
    DATA,
    RULE
};

struct Cell
{
    double data;
    enum CellType type;
    struct Cell *next;
};

// Using flexible array members (FAM)
struct Grid
{
    int size;
    struct Cell cells[];
};


struct Cell *push(struct Cell *top, double value, enum CellType type)
{
    struct Cell *newCell = (struct Cell *)malloc(sizeof(struct Cell));
    if (newCell == NULL)
    {
        printf("Memory allocation failed\n");
        exit(EXIT_FAILURE);
    }

    newCell->data = value;
    newCell->type = type;
    newCell->next = top;
    top = newCell;
    printf("inserting ram position %p\n",top);
    return top;
}

struct Grid *insert_into_grid(struct Grid *grid,int index, struct Cell top)
{
    grid->cells[index] = top;
    grid->size++;
    printf("inserting grid\n");
    return grid;
}

// Function to create a new stack(grid)
struct Grid *create_grid()
{
    struct Grid *stack = (struct Grid *)malloc(sizeof(struct Grid));
    if (stack == NULL)
    {
        perror("Memory allocation failed");
        exit(EXIT_FAILURE);
    }
    stack->size =0;
        printf("creating grid\n");
    return stack;
}

double pop(struct Cell **top)
{
    if (*top == NULL)
    {
        printf("Stack underflow\n");
        exit(EXIT_FAILURE);
    }

    struct Cell *temp = *top;
    double value = temp->data;
    enum CellType type = temp->type;
    *top = temp->next;
    free(temp);

    // Print the type and value
    if (type == DATA)
    {
        printf("Data: %.6f\n", value);
    }
    else if (type == RULE)
    {
        printf("Rule: %.6f\n", value);
    }

    return value;
}



void remove_from_grid(struct Grid *grid,int index)
{
 for (int i = index; i < grid->size - 1; i++) {
    grid->cells[i] = grid->cells[i + 1];
  }
  grid->size--;
}

struct Cell* get_grid_item(struct Grid *grid, int index)
{
    if (index >= 0 && index < grid->size) {
        return &grid->cells[index];
    } else {
        return NULL; // Handle out-of-bounds index appropriately
    }
}



// Add the cleanup function to free memory
void cleanup(struct Cell *top, struct Grid *grid) {
    while (top != NULL) {
        pop(&top);
    }
    free(grid);
}


struct Cell *read_cell(FILE *file)
{
    struct Cell *top = NULL;
    if (file == NULL)
    {
        perror("File opening failed");
        return NULL;
    }

    char buffer[8];
    int buffer_index = 0;
    int in_rule = 0;     // 0 if not in a group, 1 if in a group
    int is_negative = 0; // 0 if positive, 1 if negative

    while (1)
    {
        int character = fgetc(file);
        if (character == EOF || character == '\n' || character == ' ')
        {
            if (buffer_index > 0)
            {
                double result = 0;
                buffer[buffer_index] = '\0';
                if (in_rule)
                {
                    // printf("Rule: %s\n", buffer);
                    if (sscanf(buffer, "%lf", &result) != 1)
                    {
                        perror("Conversion error");
                        return NULL;
                    }

                    top = push(top, result, RULE);
                }
                else
                {
                    if (sscanf(buffer, "%lf", &result) != 1)
                    {
                        perror("Conversion error");
                        return NULL;
                    }

                    top = push(top, result, DATA);
                    // printf("Data: %s\n", buffer);
                }
                buffer_index = 0;
            }
            if (character == EOF)
            {
                break;
            }
            if (buffer_index >= 8)
            {
                perror("system Fault VIP_STATE(0001)");
                break;
            }

            if (character == '@')
            {
                in_rule = 1;
                is_negative = 0;
            }
            else
            {
                in_rule = 0;
            }
        }
        else if (character == '-')
        {
            is_negative = 1;
        }
        else if (character == '@')
        {
            in_rule = 1;
        }
        else if (character >= '0' && character <= '9')
        {
            if (is_negative)
            {
                buffer[buffer_index++] = '-';
                buffer[buffer_index++] = character;
            }
            else
            {
                buffer[buffer_index++] = character;
            }

            is_negative = 0;
        }
    }

    fclose(file);
    return top;
}

int main()
{
    FILE *file = fopen("1.vpx", "r");
    struct Cell *top = read_cell(file);
     struct Grid *grid = create_grid();
    struct Cell grid_cell = *top;
    grid = insert_into_grid(grid, 0, grid_cell);

    struct Cell *item = get_grid_item(grid, 0);

    // Process the elements from the grid
    while (item != NULL) {
        double element = pop(&item);
    }

    
 // Cleanup to free allocated memory
    cleanup(top, grid);

    return EXIT_SUCCESS;
}
