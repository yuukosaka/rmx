/* Generated By:JJTree: Do not edit this line. ASTDebug.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=BaseNode,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package logic.parse.SOP;

public
class ASTDebug extends SimpleNode {
  public ASTDebug(int id) {
    super(id);
  }

  public ASTDebug(parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(parserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=e84cc819a26af8dc7e3ebdda39a8d94b (do not edit this line) */
