/* Generated By:JJTree: Do not edit this line. ASTEmptyFunction.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser;

public
class ASTEmptyFunction extends JexlNode {
  public ASTEmptyFunction(int id) {
    super(id);
  }

  public ASTEmptyFunction(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=9ff7e42bd8c667f6de3437d0a573dcfc (do not edit this line) */
