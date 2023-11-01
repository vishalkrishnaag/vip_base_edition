use std::collections::HashMap;
use std::fs::File;
use std::f64;
use std::io::{BufRead,BufReader};
use std::process::exit;

enum  Cell{
    Rule(i32),
    Data(f64)
}
/*
*
 data types
    1 -> integer
    2 ->float
    3 -> string
    4 ->boolean
 */

struct VM {
    stack: Vec<Cell>,
    grid: Vec<Vec<Cell>>,
    labels: HashMap<String, (usize,usize)>,
    call_stack: Vec<usize>,
}


impl VM {
    fn new() -> VM {
        VM {
            stack: Vec::new(),
            grid: Vec::new(),
            labels: HashMap::new(),
            call_stack: Vec::new(),
        }
    }

    fn pop(&mut self) -> Option<Vec<Cell>> {
        self.grid.pop()
    }
    fn peek(&self) -> Option<&Vec<Cell>> {
        self.grid.last()
    }
    fn is_empty(&self) -> bool {
        self.grid.is_empty()
    }



    fn make_grid(&mut self, file: &mut File){
        let mut sample:Vec<Cell>= Vec::new();
        // Create a BufReader to efficiently read lines from the file
        let reader = BufReader::new(file);

        for line in reader.lines() {

            if let Ok(line) = line {
                if line.starts_with('@') {
                    // This is a group line
                    let group_name = &line[1..];
                    println!("Group: {}", group_name);
                    sample.push(Cell::Rule(group_name))
                } else {
                    // This is a point line
                    let points: Vec<f64> = line
                        .split_whitespace()
                        .map(|s| s.parse().unwrap())
                        .collect();
                    println!("Points: {:?}", points);
                    sample.push(Cell::Data(points))
                }
            }
            else {
             panic!("reading error ...");
            }
        }
        self.grid.push(sample)
    }
}

fn main() -> std::io::Result<()> {
    let mut machine:VM = VM::new();
    let mut main_file = File::open("1.vpx").unwrap();
    machine.make_grid(&mut main_file);
    Ok(())
}