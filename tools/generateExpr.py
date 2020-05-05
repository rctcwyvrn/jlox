expressions = [
    "Binary,Expr:left,Token:operator,Expr:right",
    "Grouping,Expr:expression",
    "Literal,Object:value",
    "Unary,Token:operator,Expr:right"
]

visitor = ""
def visitorAbstractFn(type):
    return "\tR visit"+type+"Expr(" + type + " expr);\n"

for node in expressions:
    parts = node.split(",")
    paramList = ",".join([x.replace(":", " ") for x in parts[1:]])

    visitor+=visitorAbstractFn(parts[0])

    # def
    classDef = "static class " + parts[0] + " extends Expr {\n"

    # constructor
    classDef += "\t" + parts[0] + "(" + paramList + ") {\n"
    for field in parts[1:]:
        name = field.split(":")[1]
        type = field.split(":")[0]
        classDef += "\t\tthis." + name + "=" +  name + ";\n"
    classDef += "\t}\n"

    #override for the visitor
    classDef += "\n"
    classDef += "\t @Override\n"
    classDef += "\t <R> R accept(Visitor<R> visitor) {\n"
    classDef += "\t\t return visitor.visit" + parts[0] + "Expr(this);\n"
    classDef += "\t}\n"

    # Fields
    for field in parts[1:]:
        name = field.split(":")[1]
        type = field.split(":")[0]
        classDef+="\tfinal " + type + " " + name + ";\n"
    classDef+="}\n"
    print(classDef)
print(visitor)