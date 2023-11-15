use std::fs::File;
use std::collections::HashMap;
use std::io::{self, BufRead};
use std::str::FromStr;
use std::fmt;
// @30 20 10 @18 @2 @31 0 @12
// @32 20 20 @33 0 @29 @2 @30 30 @18 @2 @31 0 @12
#[derive(Clone, PartialEq)]
enum CellType {
    Data,
    Rule,
}

#[derive(Clone, Debug)]
struct Cell {
    data: f64,
    cell_type: CellType,
}

#[derive(Debug)]
struct Grid {
    pc :usize,
    current_label:i64,
    current_field:usize,
    labels: HashMap<i64, usize>,
    fields:HashMap<usize,(usize,usize)>,
    call_stack:Vec<usize>,
    exception: bool,
    cells: Vec<Cell>,
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
        current_field:0,
        labels:HashMap::new(),
        fields:HashMap::new(),
        exception: false,
        call_stack:Vec::new(),
        runtime_stack: Vec::new(),
        cells: Vec::new(),
    }
}


impl Grid {
    fn evaluate_rule(&mut self, _vip_rule: i64) {
        println!("evaluating rules .. on pc {} and rule {} stack : {:?}",self.pc,_vip_rule,self.runtime_stack);
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
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
            }
            17 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(lhs >= rhs);
                self.runtime_stack.push(result);
                self.pc = self.pc+1;
            }
            23 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let rhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(f64_to_bool(lhs) != f64_to_bool(rhs));
                self.runtime_stack.push(result);
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
            }
            28 => {
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                let lhs = self.runtime_stack.pop().unwrap();
                let result: f64 = bool_to_f64(!f64_to_bool(lhs));
                self.runtime_stack.push(result);
                self.pc = self.pc+1;
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
                self.pc = self.pc+1;
            }
            29=>{
                if self.runtime_stack.is_empty() {
                    self.exception = true;
                    panic!("exception thrown");
                }
                if self.fields.is_empty(){
                    panic!("no fields declared");
                }
                let field_id :usize =self.runtime_stack.pop().unwrap() as usize;
                if let Some(target_pc) = self.fields.get(&field_id){
                    if !&self.cells.is_empty() {
                        for items in &self.cells[target_pc.0..=target_pc.1]{
                            println!("the fields are {:?}",items);
                            self.runtime_stack.push(items.data);
                        }
                    }
                }
                else {
                    panic!("method not found");
                }
                self.pc = self.pc+1;
            }

            2 => {
                // print
                println!("stack : {:?}", self.runtime_stack);
                self.pc = self.pc+1;
            }
            _ => {
                panic!("invalid instructions found {}",_vip_rule);
            }
        }
    }
    fn evaluate(&mut self) {
        let mut skip: bool = false;
        let mut skip_field:bool = false;
        let mut skip_array:bool = false;
        let mut field_begin:usize=0;
        if self.cells.is_empty() {
            panic!("container empty");
        }
        self.pc = 0;
        while self.pc < self.cells.len() {
            let (element, cell_type) = (self.cells[self.pc].data, self.cells[self.pc].cell_type.clone());
            println!("element {} {}",element,cell_type);
            if skip {
                // method end handling
                if cell_type == CellType::Rule && element == 31.0 {
                    skip = false;
                }
                self.pc=self.pc+1;
                continue;
            }

            if skip_array {
                println!("simam");
                // method end handling
                if cell_type == CellType::Rule && element == 35.0 {
                    skip_array = false;
                }
                self.pc=self.pc+1;
                continue;
            }

            if skip_field {
                if cell_type == CellType::Rule && element == 33.0 {
                    //field handling
                    skip_field = false;
                    self.fields.insert(self.current_field, (field_begin,self.pc-1));
                    self.current_field = self.current_field+1;
                }
                    self.pc=self.pc+1;
                    continue;
            }

            if cell_type == CellType::Rule && element == 30.0 {
                // method begin
                self.labels.insert(self.current_label, self.pc);
                self.current_label = self.current_label+1;
                skip = true;
                self.pc = self.pc+1;
            }
           else if cell_type==CellType::Rule && element == 32.0 {
                  self.pc = self.pc+1;
                  field_begin = self.pc;
                    skip_field = true;
            }
           else if cell_type==CellType::Rule && element == 34.0 {
               println!("skipping array {}",skip_array);
               self.pc = self.pc+1;
               skip_array = true;
           }
            else if cell_type == CellType::Rule && element == 31.0 {
                if self.call_stack.is_empty() {
                    panic!("call back error");
                }
                self.pc= self.call_stack.pop().unwrap();
                self.pc = self.pc +1;
            }
            else if cell_type == CellType::Rule && element == 12.0 {
                // call detected
                let method = self.runtime_stack.pop().unwrap() as i64;
                println!("method id is {} and labels {:?}",method,self.labels);
                if let Some(target_pc) = self.labels.get(&method){
                    self.call_stack.push(self.pc+1);
                    self.pc = *target_pc;
                    self.pc = self.pc+1;
                }
                else {
                    panic!("method not found");

                }
            }
            else if cell_type == CellType::Rule {
                //normal call
                self.evaluate_rule(element as i64);
            }
            else if cell_type == CellType::Data {
                self.runtime_stack.push(element);
                self.pc=self.pc+1;
            }
            else {
                panic!("vip_machine_exception")
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

fn read_cell(filename: &str) -> io::Result<Grid> {
    let file = File::open(filename)?;
    let reader = io::BufReader::new(file);
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
                                grid.cells.push(Cell { data: result, cell_type: CellType::Rule });
                            } else {
                                grid.cells.push(Cell { data: result, cell_type: CellType::Data });
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
                    grid.cells.push(Cell { data: result, cell_type: CellType::Rule });
                } else {
                    grid.cells.push(Cell { data: result, cell_type: CellType::Data });
                }
            } else {
                eprintln!("Conversion error");
            }
        }
    }

    Ok(grid)
}

fn main() {
    let filename = "../src/test/java/2.vpx";
    let mut grid = read_cell(filename).expect("File opening failed");
    grid.evaluate();
}
