expressions = [
    "Assign,Token:name,Expr:value",
    "Binary,Expr:left,Token:operator,Expr:right",
    "Grouping,Expr:expression",
    "Literal,Object:value",
    "Unary,Token:operator,Expr:right",
    "Var,Token:name" #Expression that returns the value stored in the variable with that name
]

statements = [
    "Expression,Expr:expression",
    "Print,Expr:expression",
    "Var,Token:name,Expr:init",
    "Block,List<Stmt>:statements"
]

stuff = [
    ("Expr", expressions),
    ("Stmt", statements)
]

def visitorAbstractFn(type, base):
    return "\tR visit"+type+base + "(" + type + " expr);\n"

for (base, expressions) in stuff:
    visitor = ""
    for node in expressions:
        parts = node.split(",")
        paramList = ",".join([x.replace(":", " ") for x in parts[1:]])

        visitor+=visitorAbstractFn(parts[0], base)

        # def
        classDef = "public static class " + parts[0] + " extends " + base + " {\n"

        # constructor
        classDef += "\t" + parts[0] + "(" + paramList + ") {\n"
        for field in parts[1:]:
            name = field.split(":")[1]
            type = field.split(":")[0]
            classDef += "\t\tthis." + name + "=" +  name + ";\n"
        classDef += "\t}\n"

        #override for the visitor
        classDef += "\n"
        classDef += "\t@Override\n"
        classDef += "\tpublic <R> R accept(Visitor<R> visitor) {\n"
        classDef += "\t\treturn visitor.visit" + parts[0] + base + "(this);\n"
        classDef += "\t}\n"

        # Fields
        for field in parts[1:]:
            name = field.split(":")[1]
            type = field.split(":")[0]
            classDef+="\tpublic final " + type + " " + name + ";\n"
        classDef+="}\n"
        print(classDef)
    print(visitor)