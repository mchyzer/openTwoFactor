/* Generated By:JJTree: Do not edit this line. ASTMapLiteral.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser;

public
class ASTMapLiteral extends JexlNode {
  public ASTMapLiteral(int id) {
    super(id);
  }

  public ASTMapLiteral(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=2c309c1d3c9a328872d8ca76f1694ebf (do not edit this line) */
