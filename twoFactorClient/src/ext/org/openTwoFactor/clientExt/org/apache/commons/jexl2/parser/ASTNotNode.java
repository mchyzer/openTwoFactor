/* Generated By:JJTree: Do not edit this line. ASTNotNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser;

public
class ASTNotNode extends JexlNode {
  public ASTNotNode(int id) {
    super(id);
  }

  public ASTNotNode(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=ee7bd28ade5e13c2a0f55a4641b6d515 (do not edit this line) */
