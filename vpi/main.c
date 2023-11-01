#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

enum ElementType {
    POINT,
    GROUP
};

struct Node {
    double data;
    enum ElementType type;
    struct Node* next;
};

struct Node* top = NULL;

void push(double value, enum ElementType type) {
    struct Node* newNode = (struct Node*)malloc(sizeof(struct Node));
    if (newNode == NULL) {
        printf("Memory allocation failed\n");
        exit(EXIT_FAILURE);
    }
    newNode->data = value;
    newNode->type = type;
    newNode->next = top;
    top = newNode;
}

double pop() {
    if (top == NULL) {
        printf("Stack underflow\n");
        exit(EXIT_FAILURE);
    }
    double value = top->data;
    enum ElementType type = top->type;
    struct Node* temp = top;
    top = top->next;
    free(temp);

    // Print the type and value
    if (type == POINT) {
        printf("Data: %.6f\n", value);
    } else if (type == GROUP) {
        printf("Rule: %.6f\n", value);
    }

    return value;
}

int main() {
    FILE* file = fopen("1.vpx", "r");
    if (file == NULL) {
        perror("File opening failed");
        return EXIT_FAILURE;
    }

    bool in_object = false;
    double current_group = 0.0;
    double current_point = 0.0;
    char character;
    char buffer[256];
    int buffer_index = 0;
    int in_group = 0;  // 0 if not in a group, 1 if in a group
    int is_negative = 0;  // 0 if positive, 1 if negative

    while (1) {
        int character = fgetc(file);
        if (character == EOF || character == '\n') {
            if (in_group) {
                printf("Rule: %s\n", buffer);
            } else {
                printf("Data: %s\n", buffer);
            }
            buffer_index = 0;
            in_group = 0;
            is_negative = 0;
            if (character == EOF) {
                break;
            }
        } else if (character == ' ' && buffer_index > 0) {
            if (in_group) {
                printf("Rule: %s\n", buffer);
            } else {
                printf("Data: %s\n", buffer);
            }
            buffer_index = 0;
            in_group = 0;
            is_negative = 0;
        } else if (character == '@') {
            if (in_group) {
                printf("Rule: %s\n", buffer);
            }
            buffer_index = 0;
            in_group = 1;
            is_negative = 0;
        } else if (character == '-') {
            is_negative = 1;
        } else if (character >= '0' && character <= '9') {
            buffer[buffer_index++] = is_negative ? '-' : character;
            is_negative = 0;
        }
    }

    fclose(file);

    // Pop and process the elements from the stack
    while (top != NULL) {
        double element = pop();
    }

    return EXIT_SUCCESS;
}
