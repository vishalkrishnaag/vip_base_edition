use std::fs::File;
use std::collections::HashMap;
use std::io::{self, BufRead};
use std::str::FromStr;
use std::fmt;
// @36 20 10 @18 @2 @37 0 @12
#[derive(Clone, PartialEq)]
enum CellType {
    Data,
    Rule,
}

#[derive(Clone)]
struct Cell {
    data: f64,
    cell_type: CellType,
}

struct Grid {
    pc :usize,
    current_label:i64,
    labels: HashMap<i64, usize>,
    call_stack:Vec<usize>,
    exception: bool,
    cells: Vec<Vec<Cell>>,
    runtime_stack: Vec<f64>,
}

impl fmt::Display for CellType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            CellType::Data => write!(f, "Data "),
            CellType::Rule => write!(f, "Rule "),
        }
    }
}

impl fmt::Debug for CellType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            CellType::Data => write!(f, "Data"),
            CellType::Rule => write!(f, "Rule"),
        }
    }
}


fn create_grid() -> Grid {
    Grid {
        pc:0,
        current_label:0,
        labels:HashMap::new(),
        exception: false,
        call_stack:Vec::new(),
        runtime_stack: Vec::new(),
        cells: Vec::new(),
    }
}


impl Grid {
    fn evaluate_rule(&mut self, _vip_rule: i64) {
        //process rule
        match _vip_rule {
            18 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let sum: f64 = lhs + rhs;
                self.runtime_stack.push(sum);
            }
            19 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let sum: f64 = lhs - rhs;
                self.runtime_stack.push(sum);
            }
            20 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let sum: f64 = lhs * rhs;
                self.runtime_stack.push(sum);
            }
            21 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let sum: f64 = lhs / rhs;
                self.runtime_stack.push(sum);
            }
            22 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(lhs > rhs);
                self.runtime_stack.push(result);
            }
            23 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(lhs >= rhs);
                self.runtime_stack.push(result);
            }
            24 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(lhs < rhs);
                self.runtime_stack.push(result);
            }
            25 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(lhs <= rhs);
                self.runtime_stack.push(result);
            }
            26 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(f64_to_bool(lhs) && f64_to_bool(rhs));
                self.runtime_stack.push(result);
            }
            27 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(f64_to_bool(lhs) || f64_to_bool(rhs));
                self.runtime_stack.push(result);
            }
            28 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(!f64_to_bool(lhs));
                self.runtime_stack.push(result);
            }
            29 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(f64_to_bool(lhs) != f64_to_bool(rhs));
                self.runtime_stack.push(result);
            }
            30 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(f64_to_bool(lhs) == f64_to_bool(rhs));
                self.runtime_stack.push(result);
            }

            2 => {
                println!("stack : {:?}", self.runtime_stack);
            }
            _ => {}
        }
    }
    fn evaluate(&mut self, index: usize) {
        let mut skip: bool = false;
        let mut method_begin_index:usize=0;
        if self.cells.is_empty() {
            panic!("container empty");
        }
        let  current_cell = &self.cells[index].clone();
        println!("starting ...{}",current_cell.len());
        self.pc = 0;
        while self.pc < current_cell.len() {
            method_begin_index = method_begin_index+1;
            let (element, cell_type) = (current_cell[self.pc].data, current_cell[self.pc].cell_type.clone());
            if skip {
                if cell_type == CellType::Rule && element == 37.0 {
                    skip = false;
                }
                self.pc=self.pc+1;
                continue;
            }

            if cell_type == CellType::Rule && element == 36.0 {
                // which is a function so skip until function ends
                self.labels.insert(self.current_label, method_begin_index);
                self.current_label = self.current_label+1;
                method_begin_index = 0;
                skip = true;
            }
            else if cell_type == CellType::Rule && element == 37.0{
                if self.call_stack.is_empty() {
                    panic!("call back error");
                }
                 self.pc= self.call_stack.pop().unwrap();
                }
            else if cell_type == CellType::Rule && element == 12.0 {
                // call detected
                let method = self.runtime_stack.pop().unwrap() as i64;
                if let Some(target_pc) = self.labels.get(&method){
                    self.call_stack.push(self.pc+1);
                    self.pc = *target_pc;
                }
                else {
                    panic!("method not found");

                }
            }
            else if cell_type == CellType::Rule {
                self.evaluate_rule(element as i64);
                self.pc=self.pc+1;
            } else {
                self.runtime_stack.push(element);
                self.pc=self.pc+1;
            }
            // Process elements
            // println!("Element: {:.6} ({:?})", element.clone(), cell_type);
        }
    }
}

fn f64_to_bool(x: f64) -> bool {
    return if x > 0.0 {
        true
    } else {
        false
    };
}
fn  bool_to_f64(x: bool) -> f64 {
    return if x == true {
        1.0
    } else {
        0.0
    };
}

fn read_cell(filename: &str, index: usize) -> io::Result<Grid> {
    let file = File::open(filename)?;
    let reader = io::BufReader::new(file);
    let mut cells = Vec::new();
    let mut grid = create_grid();
    let mut buffer = String::new();
    let mut in_rule = false;
    let mut is_negative = false;
    let mut is_float = false;

    for line in reader.lines() {
        let line = line?;
        let mut buffer_index: i64 = 0;

        for c in line.chars() {
            match c {
                ' ' | '\n' | '\t' => {
                    if buffer_index > 0 && !buffer.is_empty() {
                        // print!("buffer : {}",buffer);
                        if let Ok(result) = f64::from_str(&buffer) {
                            if in_rule {
                                cells.push(Cell { data: result, cell_type: CellType::Rule });
                            } else {
                                cells.push(Cell { data: result, cell_type: CellType::Data });
                            }
                            in_rule = false;
                            buffer.clear();
                            buffer_index = 0;
                        } else {
                            eprintln!("Conversion error {}", c);
                            break;
                        }
                    }
                }
                '@' => {
                    in_rule = true;
                    is_negative = false;
                }
                '-' => {
                    is_negative = true;
                }
                '.' => {
                    is_float = true;
                }
                _ if c.is_digit(10) => {
                    if is_negative {
                        buffer.push('-');
                    }
                    if is_float {
                        buffer.push('.');
                    }
                    buffer.push(c);
                    buffer_index += 1;
                    is_float = false;
                    is_negative = false;
                }
                _ => {
                    // Handle unexpected characters
                    return Ok(grid);
                }
            }

            if buffer_index >= 10 {
                eprintln!("system Fault VIP_STATE(0001) on {}", buffer_index);
                break;
            }
        }
        if !buffer.is_empty() {
            if let Ok(result) = f64::from_str(&buffer) {
                if in_rule {
                    cells.push(Cell { data: result, cell_type: CellType::Rule });
                } else {
                    cells.push(Cell { data: result, cell_type: CellType::Data });
                }
            } else {
                eprintln!("Conversion error");
            }
        }
    }
    grid.cells.insert(index, cells);

    Ok(grid)
}

fn main() {
    let filename = "../src/test/java/1.vpx";
    let mut grid = read_cell(filename, 0).expect("File opening failed");
    let _main_: usize = 0;
    grid.evaluate(_main_);
}
