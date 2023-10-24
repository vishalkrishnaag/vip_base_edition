use std::collections::HashMap;
use std::fs::File;
use std::{f64, io};
use std::io::{BufRead};
use std::process::exit;

#[derive(Debug)]
enum DataTypes {
    Integer(i32),
    Float(f64),
    String(String),
    Boolean(bool),
    // object is the collection of fields
    List(Vec<DataTypes>),
}
/*
*
 data types
    1 -> integer
    2 ->float
    3 -> string
    4 ->boolean
 */
impl PartialEq for DataTypes {
    fn eq(&self, other: &Self) -> bool {
        self == other
    }

    fn ne(&self, other: &Self) -> bool {
        self != other
    }
}

#[derive(Debug, PartialEq)]
enum Instruction {
    Push(i32),
    Pushf(f64),
    Pushb(bool),
    Pushs(String),
    Str(usize),
    Rlts,
    // ram latest to stack
    Sltr,
    // stack latest(top) to ram
    Rts(usize),
    // field -> field_name , skip
    Load,
    Clear(usize),
    Drop,
    Add,
    Mul,
    Div,
    Sub,
    Eq,
    Neq,
    Jeq(String),
    Jne(String),
    Gt,
    Gte,
    Lt,
    Lte,
    Or,
    And,
    Not,
    Label(String),
    // label name
    Call(String),
    Ret,
    // same as return but indicates a label ending
    Halt,
}

// fn i32_to_bool(x: i32, y: i32) -> bool {
//     x != 0 && y != 0
// }

// fn f64_to_bool(x: i64, y: i64) -> bool {
//     x != 0 && y != 0
// }

fn i32f64_to_bool(x: i32, y: i64) -> bool {
    x != 0 && y != 0
}

fn f64i32_to_bool(x: i64, y: i32) -> bool {
    x != 0 && y != 0
}

fn convert_i32tobool(x: i32) -> bool {
    if x == 1 {
        return true;
    } else {
        return false;
    }
}

fn convert_f64tobool(x: f64) -> bool {
    if x > 0.0 {
        return true;
    } else {
        return false;
    }
}

struct VM {
    stack: Vec<DataTypes>,
    program: Vec<Instruction>,
    pc: usize,
    ram: Vec<DataTypes>,
    labels: HashMap<String, (usize,usize)>,
    call_stack: Vec<usize>,
    /*
     virtual field list
     is used for
        1. object creation & mgmt
        2. getting instance of an object
        3. garbage collector
        4. separation from shared classes
    instance,stack<stack<class_id,ram Loc>>
    */
    vf_list: Vec<(i32, Vec<DataTypes>)>,
    // classId->start,end
    class_list: Vec<(usize, usize)>,
}


impl VM {
    fn new() -> VM {
        VM {
            stack: Vec::new(),
            pc: 0,
            program: Vec::new(),
            ram: Vec::new(),
            labels: HashMap::new(),
            call_stack: Vec::new(),
            vf_list: Vec::new(),
            class_list: Vec::new(),
        }
    }

    fn load_program(&mut self, file: &mut File)
    {
        let mut current_class_begin: usize = 0;
        let mut current_label:String;
        let reader = io::BufReader::new(file);
        for line in reader.lines() {
            if let Ok(line) = line {
                let mut parts: Vec<&str> = line.split_whitespace().collect();
                println!("parts are : {:?}", parts);
                if !parts.is_empty() {
                    match parts[0] {
                        "push" => {
                            if let Ok(value) = parts[1].parse::<i32>() {
                                self.program.push(Instruction::Push(value));
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }
                        "pushb" => {
                            if let Ok(value) = parts[1].parse::<bool>() {
                                self.program.push(Instruction::Pushb(value));
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }
                        "pushs" => {
                            parts.remove(0); // eat push stromg
                            let value = parts.join(" ");
                            if value.len() >= 1 {
                                self.program.push(Instruction::Pushs(value));
                            }
                        }
                        "pushf" => {
                            if let Ok(value) = parts[1].parse::<f64>() {
                                self.program.push(Instruction::Pushf(value));
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }
                        "begin" => {
                            current_class_begin = self.program.len();
                        }
                        "end" => {
                            self.class_list.push((current_class_begin, self.program.len()));
                            current_class_begin = 0;
                        }
                        "load" => self.program.push(Instruction::Load),
                        "add" => self.program.push(Instruction::Add),
                        "mul" => self.program.push(Instruction::Mul),
                        "div" => self.program.push(Instruction::Div),
                        "sub" => self.program.push(Instruction::Sub),
                        "eq" => self.program.push(Instruction::Eq),
                        "neq" => self.program.push(Instruction::Neq),
                        "gt" => self.program.push(Instruction::Gt),
                        "gte" => self.program.push(Instruction::Gte),
                        "lt" => self.program.push(Instruction::Lt),
                        "lte" => self.program.push(Instruction::Lte),
                        "and" => self.program.push(Instruction::And),
                        "or" => self.program.push(Instruction::Or),
                        "not" => self.program.push(Instruction::Not),
                        "jeq" => {
                            if let Ok(value) = parts[1].parse::<String>() {
                                self.program.push(Instruction::Jeq(value));
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }
                        "jne" => {
                            if let Ok(value) = parts[1].parse::<String>() {
                                self.program.push(Instruction::Jne(value));
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }
                        "drop" => self.program.push(Instruction::Drop),
                        "rlts" => self.program.push(Instruction::Rlts),
                        "sltr" => self.program.push(Instruction::Sltr),
                        "ret" => {
                            self.program.push(Instruction::Ret);
                            if current_label {
                                panic!("ret is not available for non label");
                            }
                             if let Some(existing_value) = self.labels.get_mut(&current_label) {
                                 existing_value.1 = self.program.len();
                            }
                             else {
                                 // Handle the case where the key doesn't exist
                                 println!("Label {} not found",current_label);
                             }

                        },
                        //todo: more jumps like jle need to implement
                        "clear" => {
                            if let Ok(value) = parts[1].parse::<usize>() {
                                self.program.push(Instruction::Clear(value))
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }

                        "str" => {
                            if let Ok(value) = parts[1].parse::<usize>() {
                                self.program.push(Instruction::Str(value));
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }
                        "rts" => {
                            if let Ok(value) = parts[1].parse::<usize>() {
                                self.program.push(Instruction::Rts(value))
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }

                        "call" => {
                            if let Ok(value) = parts[1].parse::<String>() {
                                self.program.push(Instruction::Call(value))
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }

                        "label" => {
                            if let Ok(value) = parts[1].parse::<String>() {
                                self.program.push(Instruction::Label(value.clone()));
                                self.labels.insert(value.clone(), (self.program.len(),0));
                                current_label = value;
                            } else {
                                panic!("Error parsing value on line");
                            }
                        }
                        "halt" => self.program.push(Instruction::Halt),
                        _ => {
                            panic!("Unknown instruction on line {}", line);
                        }
                    }
                }
            }
        }
    }

    fn cycle(&mut self) {
        let mut skip = false;

        for line in self.program.iter() {
            println!("calling {:?}", line);
            match line {
                Instruction::Push(value) => {
                    self.stack.push(DataTypes::Integer(*value));
                    self.pc += 1;
                }
                Instruction::Pushb(value) => {
                    self.stack.push(DataTypes::Boolean(*value));
                    self.pc += 1;
                }
                Instruction::Pushs(value) => {
                    self.stack.push(DataTypes::String(value.to_string()));
                    self.pc += 1;
                }
                Instruction::Pushf(value) => {
                    self.stack.push(DataTypes::Float(*value));
                    self.pc += 1;
                }
                Instruction::Add => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::String(x), DataTypes::String(y)) => DataTypes::String(x + &*y),
                        (DataTypes::String(x), DataTypes::Integer(y)) => DataTypes::String(format!("{}{}", x, y)),
                        (DataTypes::Integer(x), DataTypes::String(y)) => DataTypes::String(format!("{}{}", x, y)),
                        (DataTypes::String(x), DataTypes::Float(y)) => DataTypes::String(format!("{}{}", x, y)),
                        (DataTypes::Float(x), DataTypes::String(y)) => DataTypes::String(format!("{}{}", x, y)),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Integer(x + y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Float(x + f64::from(y)),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Float(f64::from(x) + y),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Float(x + y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Mul => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Integer(x * y),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Float(f64::from(x) * y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Float(x * f64::from(y)),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Float(x * y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Div => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Integer(x / y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Float(x / f64::from(y)),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Float(f64::from(x) / y),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Float(x / y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Sub => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Integer(x - y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Float(x - f64::from(y)),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Float(f64::from(x) - y),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Float(x - y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Eq => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Boolean(x), DataTypes::Boolean(y)) => DataTypes::Boolean(x == y),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Boolean(x == y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Boolean(f64i32_to_bool(x as i64, y)),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Boolean(i32f64_to_bool(x, y as i64)),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Boolean(x == y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Neq => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Boolean(x), DataTypes::Boolean(y)) => DataTypes::Boolean(x != y),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Boolean(x != y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Boolean(x != y as f64),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Boolean(x != y as i32),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Boolean(x != y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Gt => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Boolean(x), DataTypes::Boolean(y)) => DataTypes::Boolean(x > y),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Boolean(x > y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Boolean(x > y as f64),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Boolean(x > y as i32),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Boolean(x > y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Gte => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Boolean(x), DataTypes::Boolean(y)) => DataTypes::Boolean(x >= y),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Boolean(x >= y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Boolean(x >= y as f64),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Boolean(x >= y as i32),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Boolean(x >= y),
                        (_, _) => { panic!("Type mismatch in Greater than instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Lt => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Boolean(x), DataTypes::Boolean(y)) => DataTypes::Boolean(x < y),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Boolean(x < y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Boolean(x < y as f64),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Boolean(x < y as i32),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Boolean(x < y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Lte => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Boolean(x), DataTypes::Boolean(y)) => DataTypes::Boolean(x <= y),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Boolean(x <= y),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Boolean(x <= y as f64),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Boolean(x <= y as i32),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Boolean(x <= y),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::And => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Boolean(x), DataTypes::Boolean(y)) => DataTypes::Boolean(x && y),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Boolean(convert_i32tobool(x) && convert_i32tobool(y)),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Boolean(convert_f64tobool(x) && convert_i32tobool(y)),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Boolean(convert_i32tobool(x) && convert_f64tobool(y)),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Boolean(convert_f64tobool(x) && convert_f64tobool(y)),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Or => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let b = self.stack.pop().unwrap();
                    let result = match (a, b) {
                        (DataTypes::Boolean(x), DataTypes::Boolean(y)) => DataTypes::Boolean(x || y),
                        (DataTypes::Integer(x), DataTypes::Integer(y)) => DataTypes::Boolean(convert_i32tobool(x) || convert_i32tobool(y)),
                        (DataTypes::Float(x), DataTypes::Integer(y)) => DataTypes::Boolean(convert_f64tobool(x) || convert_i32tobool(y)),
                        (DataTypes::Integer(x), DataTypes::Float(y)) => DataTypes::Boolean(convert_i32tobool(x) || convert_f64tobool(y)),
                        (DataTypes::Float(x), DataTypes::Float(y)) => DataTypes::Boolean(convert_f64tobool(x) || convert_f64tobool(y)),
                        (_, _) => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Not => {
                    if self.stack.is_empty() {
                        panic!("stack empty");
                    }
                    let a = self.stack.pop().unwrap();
                    let result = match a {
                        DataTypes::Boolean(x) => DataTypes::Boolean(!x),
                        DataTypes::Integer(x) => DataTypes::Boolean(!convert_i32tobool(x)),
                        DataTypes::Float(x) => DataTypes::Boolean(!convert_f64tobool(x)),
                        _ => { panic!("Type mismatch in Add instruction") }
                    };
                    self.stack.push(result);
                    self.pc += 1;
                }
                Instruction::Jeq(value) => {
                    self.pc += 1;
                }
                Instruction::Jne(value) => {
                    self.pc += 1;
                }
                Instruction::Drop => {
                    self.stack.pop();
                    self.pc += 1;
                }
                Instruction::Clear(location) => {
                    self.ram.remove(*location);
                    self.pc += 1;
                }
                Instruction::Str(location) => {
                    if self.stack.is_empty() {
                        panic!("stack is empty");
                    }
                    self.ram.insert(*location, self.stack.pop().unwrap());
                    self.pc += 1;
                }
                Instruction::Rts(location) => {
                    let data = self.ram.get(*location);
                    match data.unwrap() {
                        DataTypes::Integer(value) => self.stack.push(DataTypes::Integer(value.clone())),
                        DataTypes::Float(value) => self.stack.push(DataTypes::Float(value.clone())),
                        DataTypes::String(value) => self.stack.push(DataTypes::String(value.clone())),
                        DataTypes::Boolean(value) => self.stack.push(DataTypes::Boolean(value.clone())),
                        _ => panic!("data empty"),
                    }

                    self.pc += 1;
                }
                Instruction::Call(label_called) => {
                    println!("total labels are {:?}",self.labels);
                    if label_called == "println"
                    {
                        println!("{:?}",self.stack.pop().unwrap());
                    } else {
                        if let Some(target_pc) = self.labels.get(&*label_called) {
                            self.call_stack.push(self.pc + 1);
                            self.pc = target_pc.0;
                        } else {
                            panic!("Label {} not found", label_called);
                        }
                    }

                    self.pc += 1;
                }
                Instruction::Ret => {
                    if let Some(return_pc) = self.call_stack.pop() {
                        self.pc = return_pc;
                    } else {
                        panic!("Return without a call");
                    }
                }
                Instruction::Label(value) => {
                    if let Some(target_pc) = self.labels.get(value) {
                        self.pc = target_pc.1;
                    } else {
                        panic!("Label {} not found", value);
                    }
                    self.pc += 1;
                }

                Instruction::Halt => {
                    exit(0);
                }
                _ => {
                    panic!("Unknown instruction {:?}", self.program.get(self.pc + 1));
                }
            }
        }
    }
}

fn main() -> std::io::Result<()> {
    let mut file = File::open("../src/test/java/output.vpx").unwrap();
    let mut machine: VM = VM::new();

    machine.load_program(&mut file);
    machine.pc = 0;
    machine.cycle();
    Ok(())
}