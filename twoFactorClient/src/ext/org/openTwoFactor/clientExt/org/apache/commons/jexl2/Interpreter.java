/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openTwoFactor.clientExt.org.apache.commons.jexl2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openTwoFactor.clientExt.org.apache.commons.jexl2.introspection.JexlMethod;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.introspection.JexlPropertyGet;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.introspection.JexlPropertySet;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.introspection.Uberspect;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTAdditiveNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTAdditiveOperator;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTAmbiguous;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTAndNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTArrayAccess;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTArrayLiteral;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTAssignment;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTBitwiseAndNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTBitwiseComplNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTBitwiseOrNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTBitwiseXorNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTBlock;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTConstructorNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTDivNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTEQNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTERNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTEmptyFunction;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTFalseNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTFloatLiteral;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTForeachStatement;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTFunctionNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTGENode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTGTNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTIdentifier;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTIfStatement;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTIntegerLiteral;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTJexlScript;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTLENode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTLTNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTMapEntry;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTMapLiteral;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTMethodNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTModNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTMulNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTNENode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTNRNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTNotNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTNullLiteral;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTOrNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTReference;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTSizeFunction;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTSizeMethod;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTStringLiteral;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTTernaryNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTTrueNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTUnaryMinusNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ASTWhileStatement;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.JexlNode;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.Node;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.ParserVisitor;
import org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser.SimpleNode;
import org.openTwoFactor.clientExt.org.apache.commons.logging.Log;




/**
 * An interpreter of JEXL syntax.
 *
 * @since 2.0
 */
public class Interpreter implements ParserVisitor {
    /** The logger. */
    protected final Log logger;
    /** The uberspect. */
    protected final Uberspect uberspect;
    /** The arithmetic handler. */
    protected final JexlArithmetic arithmetic;
    /** The map of registered functions. */
    protected final Map<String, Object> functions;
    /** The map of registered functions. */
    protected Map<String, Object> functors;
    /** The context to store/retrieve variables. */
    protected final JexlContext context;
    /** Strict interpreter flag. */
    protected final boolean strict;
    /** Silent intepreter flag. */
    protected boolean silent;
    /** Cache executors. */
    protected final boolean  cache;
    /** Registers made of 2 pairs of {register-name, value}. */
    protected Object[] registers = null;
    /** Empty parameters for method matching. */
    protected static final Object[] EMPTY_PARAMS = new Object[0];

    /**
     * Creates an interpreter.
     * @param jexl the engine creating this interpreter
     * @param aContext the context to evaluate expression
     */
    public Interpreter(JexlEngine jexl, JexlContext aContext) {
        this.logger = jexl.logger;
        this.uberspect = jexl.uberspect;
        this.arithmetic = jexl.arithmetic;
        this.functions = jexl.functions;
        this.strict = !this.arithmetic.isLenient();
        this.silent = jexl.silent;
        this.cache = jexl.cache != null;
        this.context = aContext;
        this.functors = null;
    }

    /**
     * Sets whether this interpreter throws JexlException during evaluation.
     * @param flag true means no JexlException will be thrown but will be logged
     *        as info through the Jexl engine logger, false allows them to be thrown.
     */
    public void setSilent(boolean flag) {
        this.silent = flag;
    }

    /**
     * Checks whether this interpreter throws JexlException during evaluation.
     * @return true if silent, false otherwise
     */
    public boolean isSilent() {
        return this.silent;
    }

    /**
     * Interpret the given script/expression.
     * <p>
     * If the underlying JEXL engine is silent, errors will be logged through its logger as info.
     * </p>
     * @param node the script or expression to interpret.
     * @return the result of the interpretation.
     * @throws JexlException if any error occurs during interpretation.
     */
    public Object interpret(JexlNode node) {
        try {
            return node.jjtAccept(this, null);
        } catch (JexlException xjexl) {
            if (silent) {
                logger.warn(xjexl.getMessage(), xjexl.getCause());
                return null;
            }
            throw xjexl;
        }
    }

    /**
     * Gets the uberspect.
     * @return an {@link Uberspect}
     */
    protected Uberspect getUberspect() {
        return uberspect;
    }

    /**
     * Sets this interpreter registers for bean access/assign expressions.
     * @param theRegisters the array of registers
     */
    protected void setRegisters(Object... theRegisters) {
        this.registers = theRegisters;
    }

    /**
     * Finds the node causing a NPE for diadic operators.
     * @param xrt the RuntimeException
     * @param node the parent node
     * @param left the left argument
     * @param right the right argument
     * @return the left, right or parent node
     */
    protected JexlNode findNullOperand(RuntimeException xrt, JexlNode node, Object left, Object right) {
        if (xrt instanceof NullPointerException
                && JexlException.NULL_OPERAND == xrt.getMessage()) {
            if (left == null) {
                return node.jjtGetChild(0);
            }
            if (right == null) {
                return node.jjtGetChild(1);
            }
        }
        return node;
    }

    /**
     * Triggered when variable can not be resolved.
     * @param xjexl the JexlException ("undefined variable " + variable)
     * @return throws JexlException if strict, null otherwise
     */
    protected Object unknownVariable(JexlException xjexl) {
        if (strict) {
            throw xjexl;
        }
        if (!silent) {
            logger.warn(xjexl.getMessage());
        }
        return null;
    }

    /**
     * Triggered when method, function or constructor invocation fails.
     * @param xjexl the JexlException wrapping the original error
     * @return throws JexlException if strict, null otherwise
     */
    protected Object invocationFailed(JexlException xjexl) {
        if (strict) {
            throw xjexl;
        }
        if (!silent) {
            logger.warn(xjexl.getMessage(), xjexl.getCause());
        }
        return null;
    }

    /**
     * Resolves a namespace, eventually allocating an instance using context as constructor argument.
     * The lifetime of such instances span the current expression or script evaluation.
     *
     * @param prefix the prefix name (may be null for global namespace)
     * @param node the AST node
     * @return the namespace instance
     */
    protected Object resolveNamespace(String prefix, JexlNode node) {
        Object namespace;
        // check whether this namespace is a functor
        if (functors != null) {
            namespace = functors.get(prefix);
            if (namespace != null) {
                return namespace;
            }
        }
        namespace = functions.get(prefix);
        if (namespace == null) {
            throw new JexlException(node, "no such function namespace " + prefix);
        }
        // allow namespace to be instantiated as functor with context
        if (namespace instanceof Class<?>) {
            Object[] args = new Object[]{context};
            Constructor<?> ctor = uberspect.getConstructor(namespace,args, node);
            if (ctor != null) {
                try {
                    namespace = ctor.newInstance(args);
                    if (functors == null) {
                        functors = new HashMap<String, Object>();
                    }
                    functors.put(prefix, namespace);
                } catch (Exception xinst) {
                    throw new JexlException(node, "unable to instantiate namespace " + prefix, xinst);
                }
            }
        }
        return namespace;
    }

    /** {@inheritDoc} */
    public Object visit(ASTAdditiveNode node, Object data) {
        /**
         * The pattern for exception mgmt is to let the child*.jjtAccept
         * out of the try/catch loop so that if one fails, the ex will
         * traverse up to the interpreter.
         * In cases where this is not convenient/possible, JexlException must
         * be caught explicitly and rethrown.
         */
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        for(int c = 2, size = node.jjtGetNumChildren(); c < size; c += 2) {
            Object right = node.jjtGetChild(c).jjtAccept(this, data);
            try {
                JexlNode op = node.jjtGetChild(c - 1);
                if (op instanceof ASTAdditiveOperator) {
                    String which = ((ASTAdditiveOperator) op).image;
                    if ("+".equals(which)) {
                        left = arithmetic.add(left, right);
                        continue;
                    }
                    if ("-".equals(which)) {
                        left = arithmetic.subtract(left, right);
                        continue;
                    }
                    throw new UnsupportedOperationException("unknown operator " + which);
                }
                throw new IllegalArgumentException("unknown operator " + op);
            } catch (RuntimeException xrt) {
                JexlNode xnode = findNullOperand(xrt, node, left, right);
                throw new JexlException(xnode, "+/- error", xrt);
            }
        }
        return left;
    }

    /** {@inheritDoc} */
    public Object visit(ASTAdditiveOperator node, Object data) {
        throw new UnsupportedOperationException("Shoud not be called.");
    }

    /** {@inheritDoc} */
    public Object visit(ASTAndNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            boolean leftValue = arithmetic.toBoolean(left);
            if (!leftValue) {
                return Boolean.FALSE;
            }
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(0), "boolean coercion error", xrt);
        }
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            boolean rightValue = arithmetic.toBoolean(right);
            if (!rightValue) {
                return Boolean.FALSE;
            }
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(1), "boolean coercion error", xrt);
        }
        return Boolean.TRUE;
    }

    /** {@inheritDoc} */
    public Object visit(ASTArrayAccess node, Object data) {
        // first objectNode is the identifier
        Object object = node.jjtGetChild(0).jjtAccept(this, data);
        // can have multiple nodes - either an expression, integer literal or
        // reference
        int numChildren = node.jjtGetNumChildren();
        for (int i = 1; i < numChildren; i++) {
            JexlNode nindex = node.jjtGetChild(i);
            if (nindex instanceof JexlNode.Literal<?>) {
                object = nindex.jjtAccept(this, object);
            } else {
                Object index = nindex.jjtAccept(this, null);
                object = getAttribute(object, index, nindex);
            }
        }

        return object;
    }

    /** {@inheritDoc} */
    public Object visit(ASTArrayLiteral node, Object data) {
        Object literal = node.getLiteral();
        if (literal == null) {
            int childCount = node.jjtGetNumChildren();
            Object[] array = new Object[childCount];
            for (int i = 0; i < childCount; i++) {
                Object entry = node.jjtGetChild(i).jjtAccept(this, data);
                array[i] = entry;
            }
            literal = arithmetic.narrowArrayType(array);
            node.setLiteral(literal);
        }
        return literal;
    }
    
    /** {@inheritDoc} */
    public Object visit(ASTAssignment node, Object data) {
        // left contains the reference to assign to
        JexlNode left = node.jjtGetChild(0);
        if (!(left instanceof ASTReference)) {
            throw new JexlException(left, "illegal assignment form");
        }
        // right is the value expression to assign
        Object right = node.jjtGetChild(1).jjtAccept(this, data);

        // determine initial object & property:
        JexlNode objectNode = null;
        Object object = null;
        JexlNode propertyNode = null;
        Object property = null;
        boolean isVariable = true;
        int v = 0;
        StringBuilder variableName = null;
        // 1: follow children till penultimate
        int last = left.jjtGetNumChildren() - 1;
        for (int c = 0; c < last; ++c) {
            objectNode = left.jjtGetChild(c);
            // evaluate the property within the object
            object = objectNode.jjtAccept(this, object);
            if (object != null) {
                continue;
            }
            isVariable &= objectNode instanceof ASTIdentifier;
            // if we get null back as a result, check for an ant variable
            if (isVariable) {
                if (v == 0) {
                    variableName = new StringBuilder(left.jjtGetChild(0).image);
                    v = 1;
                }
                for(; v <= c; ++v) {
                    variableName.append('.');
                    variableName.append(left.jjtGetChild(v).image);
                }
                object = context.get(variableName.toString());
                // disallow mixing ant & bean with same root; avoid ambiguity
                if (object != null) {
                    isVariable = false;
                }
            } else {
                throw new JexlException(objectNode, "illegal assignment form");
            }
        }
        // 2: last objectNode will perform assignement in all cases
        propertyNode = left.jjtGetChild(last);
        boolean antVar = false;
        if (propertyNode instanceof ASTIdentifier) {
            property = ((ASTIdentifier) propertyNode).image;
            antVar = true;
        } else if (propertyNode instanceof ASTIntegerLiteral) {
            property = ((ASTIntegerLiteral) propertyNode).getLiteral();
            antVar = true;
        } else if (propertyNode instanceof ASTArrayAccess) {
            // first objectNode is the identifier
            objectNode = propertyNode;
            ASTArrayAccess narray = (ASTArrayAccess) objectNode;
            Object nobject = narray.jjtGetChild(0).jjtAccept(this, object);
            if (nobject == null) {
                throw new JexlException(objectNode, "array element is null");
            } else {
                object = nobject;
            }
            // can have multiple nodes - either an expression, integer literal or
            // reference
            last = narray.jjtGetNumChildren() - 1;
            for (int i = 1; i < last; i++) {
                objectNode = narray.jjtGetChild(i);
                if (objectNode instanceof JexlNode.Literal<?>) {
                    object = objectNode.jjtAccept(this, object);
                } else {
                    Object index = objectNode.jjtAccept(this, null);
                    object = getAttribute(object, index, objectNode);
                }
            }
            property = narray.jjtGetChild(last).jjtAccept(this, null);
        } else {
            throw new JexlException(objectNode, "illegal assignment form");
        }
        // deal with ant variable; set context
        if (antVar) {
            if (isVariable && object == null) {
                if (variableName != null) {
                    if (last > 0) {
                        variableName.append('.');
                    }
                    variableName.append(property);
                    property = variableName.toString();
                }
                context.set(String.valueOf(property), right);
                return right;
            }
        }
        if (property == null) {
            // no property, we fail
            throw new JexlException(propertyNode, "property is null");
        }
        if (object == null) {
            // no object, we fail
            throw new JexlException(objectNode, "bean is null");
        }
        // one before last, assign
        setAttribute(object, property, right, propertyNode);
        return right;
    }

    /** {@inheritDoc} */
    public Object visit(ASTBitwiseAndNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        int n = 0;
        // coerce these two values longs and 'and'.
        try {
            long l = arithmetic.toLong(left);
            n = 1;
            long r = arithmetic.toLong(right);
            return Long.valueOf(l & r);
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(n), "long coercion error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTBitwiseComplNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            long l = arithmetic.toLong(left);
            return Long.valueOf(~l);
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(0), "long coercion error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTBitwiseOrNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        int n = 0;
        // coerce these two values longs and 'or'.
        try {
            long l = arithmetic.toLong(left);
            n = 1;
            long r = arithmetic.toLong(right);
            return Long.valueOf(l | r);
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(n), "long coercion error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTBitwiseXorNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        int n = 0;
        // coerce these two values longs and 'xor'.
        try {
            long l = arithmetic.toLong(left);
            n = 1;
            long r = arithmetic.toLong(right);
            return Long.valueOf(l ^ r);
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(n), "long coercion error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTBlock node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        Object result = null;
        for (int i = 0; i < numChildren; i++) {
            result = node.jjtGetChild(i).jjtAccept(this, data);
        }
        return result;
    }

    /** {@inheritDoc} */
    public Object visit(ASTDivNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.divide(left, right);
        } catch (RuntimeException xrt) {
            if (!strict && xrt instanceof ArithmeticException) {
                return new Double(0.0);
            }
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "divide error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTEmptyFunction node, Object data) {
        Object o = node.jjtGetChild(0).jjtAccept(this, data);
        if (o == null) {
            return Boolean.TRUE;
        }
        if (o instanceof String && "".equals(o)) {
            return Boolean.TRUE;
        }
        if (o.getClass().isArray() && ((Object[]) o).length == 0) {
            return Boolean.TRUE;
        }
        if (o instanceof Collection<?>) {
            return ((Collection<?>) o).isEmpty()? Boolean.TRUE : Boolean.FALSE;
        }
        // Map isn't a collection
        if (o instanceof Map<?, ?>) {
            return ((Map<?,?>) o).isEmpty()? Boolean.TRUE : Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    /** {@inheritDoc} */
    public Object visit(ASTEQNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.equals(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (RuntimeException xrt) {
            throw new JexlException(node, "== error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTFalseNode node, Object data) {
        return Boolean.FALSE;
    }

    /** {@inheritDoc} */
    public Object visit(ASTFloatLiteral node, Object data) {
        if (data != null) {
            return getAttribute(data, node.getLiteral(), node);
        }
        return node.getLiteral();
    }

    /** {@inheritDoc} */
    public Object visit(ASTForeachStatement node, Object data) {
        Object result = null;
        /* first objectNode is the loop variable */
        ASTReference loopReference = (ASTReference) node.jjtGetChild(0);
        ASTIdentifier loopVariable = (ASTIdentifier) loopReference.jjtGetChild(0);
        /* second objectNode is the variable to iterate */
        Object iterableValue = node.jjtGetChild(1).jjtAccept(this, data);
        // make sure there is a value to iterate on and a statement to execute
        if (iterableValue != null && node.jjtGetNumChildren() >= 3) {
            /* third objectNode is the statement to execute */
            JexlNode statement = node.jjtGetChild(2);
            // get an iterator for the collection/array etc via the
            // introspector.
            Iterator<?> itemsIterator = getUberspect().getIterator(iterableValue, node);
            if (itemsIterator != null) {
                while (itemsIterator.hasNext()) {
                    // set loopVariable to value of iterator
                    Object value = itemsIterator.next();
                    context.set(loopVariable.image, value);
                    // execute statement
                    result = statement.jjtAccept(this, data);
                }
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    public Object visit(ASTGENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.greaterThanOrEqual(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (RuntimeException xrt) {
            throw new JexlException(node, ">= error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTGTNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.greaterThan(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (RuntimeException xrt) {
            throw new JexlException(node, "> error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTERNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.matches(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (RuntimeException xrt) {
            throw new JexlException(node, "=~ error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTIdentifier node, Object data) {
        String name = node.image;
        if (data == null) {
            if (registers != null) {
                return registers[name.charAt(1) - '0'];
            }
            Object value = context.get(name);
            if (value == null
                && !(node.jjtGetParent() instanceof ASTReference)
                && !context.has(name)) {
                JexlException xjexl = new JexlException(node, "undefined variable " + name);
                return unknownVariable(xjexl);
            }
            return value;
        } else {
            return getAttribute(data, name, node);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTIfStatement node, Object data) {
        int n = 0;
        try {
            Object result = null;
            /* first objectNode is the expression */
            Object expression = node.jjtGetChild(0).jjtAccept(this, data);
            if (arithmetic.toBoolean(expression)) {
                // first objectNode is true statement
                n = 1;
                result = node.jjtGetChild(1).jjtAccept(this, data);
            } else {
                // if there is a false, execute it. false statement is the second
                // objectNode
                if (node.jjtGetNumChildren() == 3) {
                    n = 2;
                    result = node.jjtGetChild(2).jjtAccept(this, data);
                }
            }
            return result;
        } catch (JexlException error) {
            throw error;
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(n), "if error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTIntegerLiteral node, Object data) {
        if (data != null) {
            return getAttribute(data, node.getLiteral(), node);
        }
        return node.getLiteral();
    }

    /** {@inheritDoc} */
    public Object visit(ASTJexlScript node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        Object result = null;
        for (int i = 0; i < numChildren; i++) {
            JexlNode child = node.jjtGetChild(i);
            result = child.jjtAccept(this, data);
        }
        return result;
    }

    /** {@inheritDoc} */
    public Object visit(ASTLENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.lessThanOrEqual(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (RuntimeException xrt) {
            throw new JexlException(node, "<= error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTLTNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.lessThan(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (RuntimeException xrt) {
            throw new JexlException(node, "< error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTMapEntry node, Object data) {
        Object key = node.jjtGetChild(0).jjtAccept(this, data);
        Object value = node.jjtGetChild(1).jjtAccept(this, data);
        return new Object[]{key, value};
    }

    /** {@inheritDoc} */
    public Object visit(ASTMapLiteral node, Object data) {
        int childCount = node.jjtGetNumChildren();
        Map<Object, Object> map = new HashMap<Object, Object>();

        for (int i = 0; i < childCount; i++) {
            Object[] entry = (Object[]) (node.jjtGetChild(i)).jjtAccept(this, data);
            map.put(entry[0], entry[1]);
        }

        return map;
    }

    /** {@inheritDoc} */
    public Object visit(ASTMethodNode node, Object data) {
        // the object to invoke the method on should be in the data argument
        if (data == null) {
            // if the first child of the (ASTReference) parent,
            // it is considered as calling a 'top level' function
            if (node.jjtGetParent().jjtGetChild(0) == node) {
                data = resolveNamespace(null, node);
                if (data == null) {
                    throw new JexlException(node, "no default function namespace");
                }
            } else {
                throw new JexlException(node, "attempting to call method on null");
            }
        }
        // objectNode 0 is the identifier (method name), the others are parameters.
        String methodName = ((ASTIdentifier) node.jjtGetChild(0)).image;

        // get our arguments
        int argc = node.jjtGetNumChildren() - 1;
        Object[] argv = new Object[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = node.jjtGetChild(i + 1).jjtAccept(this, null);
        }

        JexlException xjexl = null;
        try {
            // attempt to reuse last executor cached in volatile JexlNode.value
            if (cache) {
                Object cached = node.jjtGetValue();
                if (cached instanceof JexlMethod) {
                    JexlMethod me = (JexlMethod) cached;
                    Object eval = me.tryInvoke(methodName, data, argv);
                    if (!me.tryFailed(eval)) {
                        return eval;
                    }
                }
            }
            JexlMethod vm = uberspect.getMethod(data, methodName, argv, node);
            // DG: If we can't find an exact match, narrow the parameters and try again!
            if (vm == null) {
                if (arithmetic.narrowArguments(argv)) {
                    vm = uberspect.getMethod(data, methodName, argv, node);
                }
                if (vm == null) {
                    xjexl = new JexlException(node, "unknown or ambiguous method", null);
                }
            }
            if (xjexl == null) {
                Object eval = vm.invoke(data, argv); // vm cannot be null if xjexl is null
                // cache executor in volatile JexlNode.value
                if (cache && vm.isCacheable()) {
                    node.jjtSetValue(vm);
                }
                return eval;
            }
        } catch (InvocationTargetException e) {
            xjexl = new JexlException(node, "method invocation error", e.getCause());
        } catch (Exception e) {
            xjexl = new JexlException(node, "method error", e);
        }
        return invocationFailed(xjexl);
    }

    /** {@inheritDoc} */
    public Object visit(ASTConstructorNode node, Object data) {
        // first child is class or class name
        Object cobject = node.jjtGetChild(0).jjtAccept(this, data);
        // get the ctor args
        int argc = node.jjtGetNumChildren() - 1;
        Object[] argv = new Object[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = node.jjtGetChild(i + 1).jjtAccept(this, null);
        }

        JexlException xjexl = null;
        try {
            Constructor<?> ctor = uberspect.getConstructor(cobject, argv, node);
            // DG: If we can't find an exact match, narrow the parameters and
            // try again!
            if (ctor == null) {
                if (arithmetic.narrowArguments(argv)) {
                    ctor = uberspect.getConstructor(cobject, argv, node);
                }
                if (ctor == null) {
                    xjexl = new JexlException(node, "unknown constructor", null);
                }
            }
            if (xjexl == null) {
                return ctor.newInstance(argv);
            }
        } catch (InvocationTargetException e) {
            xjexl = new JexlException(node, "constructor invocation error", e.getCause());
        } catch (Exception e) {
            xjexl = new JexlException(node, "constructor error", e);
        }
        return invocationFailed(xjexl);
    }

    /** {@inheritDoc} */
    public Object visit(ASTFunctionNode node, Object data) {
        // objectNode 0 is the prefix
        String prefix = ((ASTIdentifier) node.jjtGetChild(0)).image;
        Object namespace = resolveNamespace(prefix, node);
        // objectNode 1 is the identifier , the others are parameters.
        String function = ((ASTIdentifier) node.jjtGetChild(1)).image;

        // get our args
        int argc = node.jjtGetNumChildren() - 2;
        Object[] argv = new Object[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = node.jjtGetChild(i + 2).jjtAccept(this, null);
        }

        JexlException xjexl = null;
        try {
            // attempt to reuse last executor cached in volatile JexlNode.value
            if (cache) {
                Object cached = node.jjtGetValue();
                if (cached instanceof JexlMethod) {
                    JexlMethod me = (JexlMethod) cached;
                    Object eval = me.tryInvoke(function, namespace, argv);
                    if (!me.tryFailed(eval)) {
                        return eval;
                    }
                }
            }
            JexlMethod vm = uberspect.getMethod(namespace, function, argv, node);
            // DG: If we can't find an exact match, narrow the parameters and
            // try again!
            if (vm == null) {
                // replace all numbers with the smallest type that will fit
                if (arithmetic.narrowArguments(argv)) {
                    vm = uberspect.getMethod(namespace, function, argv, node);
                }
                if (vm == null) {
                    xjexl = new JexlException(node, "unknown function", null);
                }
            }
            if (xjexl == null) {
                Object eval = vm.invoke(namespace, argv); // vm cannot be null if xjexl is null
                // cache executor in volatile JexlNode.value
                if (cache && vm.isCacheable()) {
                    node.jjtSetValue(vm);
                }
                return eval;
            }
        } catch (InvocationTargetException e) {
            xjexl = new JexlException(node, "function invocation error", e.getCause());
        } catch (Exception e) {
            xjexl = new JexlException(node, "function error", e);
        }
        return invocationFailed(xjexl);
    }

    /** {@inheritDoc} */
    public Object visit(ASTModNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.mod(left, right);
        } catch (RuntimeException xrt) {
            if (!strict && xrt instanceof ArithmeticException) {
                return new Double(0.0);
            }
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "% error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTMulNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.multiply(left, right);
        } catch (RuntimeException xrt) {
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "* error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTNENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.equals(left, right) ? Boolean.FALSE : Boolean.TRUE;
        } catch (RuntimeException xrt) {
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "!= error", xrt);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTNRNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.matches(left, right) ? Boolean.FALSE : Boolean.TRUE;
        } catch (RuntimeException xrt) {
            throw new JexlException(node, "!~ error", xrt);
        }
    }
    
    /** {@inheritDoc} */
    public Object visit(ASTNotNode node, Object data) {
        Object val = node.jjtGetChild(0).jjtAccept(this, data);
        return arithmetic.toBoolean(val) ? Boolean.FALSE : Boolean.TRUE;
    }

    /** {@inheritDoc} */
    public Object visit(ASTNullLiteral node, Object data) {
        return null;
    }

    /** {@inheritDoc} */
    public Object visit(ASTOrNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            boolean leftValue = arithmetic.toBoolean(left);
            if (leftValue) {
                return Boolean.TRUE;
            }
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(0), "boolean coercion error", xrt);
        }
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            boolean rightValue = arithmetic.toBoolean(right);
            if (rightValue) {
                return Boolean.TRUE;
            }
        } catch (RuntimeException xrt) {
            throw new JexlException(node.jjtGetChild(1), "boolean coercion error", xrt);
        }
        return Boolean.FALSE;
    }

    /** {@inheritDoc} */
    public Object visit(ASTReference node, Object data) {
        // could be array access, identifier or map literal
        // followed by zero or more ("." and array access, method, size,
        // identifier or integer literal)

        int numChildren = node.jjtGetNumChildren();

        // pass first piece of data in and loop through children
        Object result = null;
        StringBuilder variableName = null;
        boolean isVariable = true;
        int v = 0;
        for (int c = 0; c < numChildren; c++) {
            JexlNode theNode = node.jjtGetChild(c);
            // integer literals may be part of an antish var name only if no bean was found so far
            if (result == null && theNode instanceof ASTIntegerLiteral) {
                isVariable &= v > 0;
            } else {
                isVariable &= (theNode instanceof ASTIdentifier);
                result = theNode.jjtAccept(this, result);
            }
            // if we get null back a result, check for an ant variable
            if (result == null && isVariable) {
                if (v == 0) {
                    variableName = new StringBuilder(node.jjtGetChild(0).image);
                    v = 1;
                }
                for (; v <= c; ++v) {
                    variableName.append('.');
                    variableName.append(node.jjtGetChild(v).image);
                }
                result = context.get(variableName.toString());
            }
        }
        if (result == null) {
            if (isVariable
                    && !(node.jjtGetParent() instanceof ASTTernaryNode)
                    && !context.has(variableName.toString())) {
                JexlException xjexl = new JexlException(node, "undefined variable " + variableName.toString());
                return unknownVariable(xjexl);
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    public Object visit(ASTSizeFunction node, Object data) {
        Object val = node.jjtGetChild(0).jjtAccept(this, data);

        if (val == null) {
            throw new JexlException(node, "size() : argument is null", null);
        }

        return Integer.valueOf(sizeOf(node, val));
    }

    /** {@inheritDoc} */
    public Object visit(ASTSizeMethod node, Object data) {
        return Integer.valueOf(sizeOf(node, data));
    }

    /** {@inheritDoc} */
    public Object visit(ASTStringLiteral node, Object data) {
        if (data != null) {
            return getAttribute(data, node.getLiteral(), node);
        }
        return node.image;
    }

    /** {@inheritDoc} */
    public Object visit(ASTTernaryNode node, Object data) {
        Object condition = node.jjtGetChild(0).jjtAccept(this, data);
        if (node.jjtGetNumChildren() == 3) {
            if (condition != null && arithmetic.toBoolean(condition)) {
                return node.jjtGetChild(1).jjtAccept(this, data);
            } else {
                return node.jjtGetChild(2).jjtAccept(this, data);
            }
        }
        if (condition != null && !Boolean.FALSE.equals(condition)) {
            return condition;
        } else {
            return node.jjtGetChild(1).jjtAccept(this, data);
        }
    }

    /** {@inheritDoc} */
    public Object visit(ASTTrueNode node, Object data) {
        return Boolean.TRUE;
    }

    /** {@inheritDoc} */
    public Object visit(ASTUnaryMinusNode node, Object data) {
        JexlNode valNode = node.jjtGetChild(0);
        Object val = valNode.jjtAccept(this, data);
        if (val instanceof Byte) {
            byte valueAsByte = ((Byte) val).byteValue();
            return Byte.valueOf((byte) -valueAsByte);
        } else if (val instanceof Short) {
            short valueAsShort = ((Short) val).shortValue();
            return Short.valueOf((short) -valueAsShort);
        } else if (val instanceof Integer) {
            int valueAsInt = ((Integer) val).intValue();
            return Integer.valueOf(-valueAsInt);
        } else if (val instanceof Long) {
            long valueAsLong = ((Long) val).longValue();
            return Long.valueOf(-valueAsLong);
        } else if (val instanceof Float) {
            float valueAsFloat = ((Float) val).floatValue();
            return new Float(-valueAsFloat);
        } else if (val instanceof Double) {
            double valueAsDouble = ((Double) val).doubleValue();
            return new Double(-valueAsDouble);
        } else if (val instanceof BigDecimal) {
            BigDecimal valueAsBigD = (BigDecimal) val;
            return valueAsBigD.negate();
        } else if (val instanceof BigInteger) {
            BigInteger valueAsBigI = (BigInteger) val;
            return valueAsBigI.negate();
        }
        throw new JexlException(valNode, "not a number");
    }

    /** {@inheritDoc} */
    public Object visit(ASTWhileStatement node, Object data) {
        Object result = null;
        /* first objectNode is the expression */
        Node expressionNode = node.jjtGetChild(0);
        while (arithmetic.toBoolean(expressionNode.jjtAccept(this, data))) {
            // execute statement
            result = node.jjtGetChild(1).jjtAccept(this, data);
        }

        return result;
    }

    /**
     * Calculate the <code>size</code> of various types: Collection, Array,
     * Map, String, and anything that has a int size() method.
     * @param node the node that gave the value to size
     * @param val the object to get the size of.
     * @return the size of val
     */
    private int sizeOf(JexlNode node, Object val) {
        if (val instanceof Collection<?>) {
            return ((Collection<?>) val).size();
        } else if (val.getClass().isArray()) {
            return Array.getLength(val);
        } else if (val instanceof Map<?, ?>) {
            return ((Map<?, ?>) val).size();
        } else if (val instanceof String) {
            return ((String) val).length();
        } else {
            // check if there is a size method on the object that returns an
            // integer and if so, just use it
            Object[] params = new Object[0];
            JexlMethod vm = uberspect.getMethod(val, "size", EMPTY_PARAMS, node);
            if (vm != null && vm.getReturnType() == Integer.TYPE) {
                Integer result;
                try {
                    result = (Integer) vm.invoke(val, params);
                } catch (Exception e) {
                    throw new JexlException(node, "size() : error executing", e);
                }
                return result.intValue();
            }
            throw new JexlException(node, "size() : unsupported type : " + val.getClass(), null);
        }
    }

    /**
     * Gets an attribute of an object.
     *
     * @param object to retrieve value from
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or
     *            key for a map
     * @return the attribute value
     */
    public Object getAttribute(Object object, Object attribute) {
        return getAttribute(object, attribute, null);
    }

    /**
     * Gets an attribute of an object.
     *
     * @param object to retrieve value from
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or
     *            key for a map
     * @param node the node that evaluated as the object
     * @return the attribute value
     */
    protected Object getAttribute(Object object, Object attribute, JexlNode node) {
        if (object == null) {
            throw new JexlException(node, "object is null");
        }
        // attempt to reuse last executor cached in volatile JexlNode.value
        if (node != null && cache) {
            Object cached = node.jjtGetValue();
            if (cached instanceof JexlPropertyGet) {
                JexlPropertyGet vg = (JexlPropertyGet) cached;
                Object value = vg.tryInvoke(object, attribute);
                if (!vg.tryFailed(value)) {
                    return value;
                }
            }
        }
        JexlPropertyGet vg = uberspect.getPropertyGet(object, attribute, node);
        if (vg != null) {
            try {
                Object value = vg.invoke(object);
                // cache executor in volatile JexlNode.value
                if (node != null && cache && vg.isCacheable()) {
                    node.jjtSetValue(vg);
                }
                return value;
            } catch (Exception xany) {
                if (node == null) {
                    throw new RuntimeException(xany);
                } else {
                    JexlException xjexl = new JexlException(node, "get object property error", xany);
                    if (strict) {
                        throw xjexl;
                    }
                    if (!silent) {
                        logger.warn(xjexl.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * Sets an attribute of an object.
     *
     * @param object to set the value to
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or
     *            key for a map
     * @param value the value to assign to the object's attribute
     */
    public void setAttribute(Object object, Object attribute, Object value) {
        setAttribute(object, attribute, value, null);
    }

    /**
     * Sets an attribute of an object.
     *
     * @param object to set the value to
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or
     *            key for a map
     * @param value the value to assign to the object's attribute
     * @param node the node that evaluated as the object
     */
    protected void setAttribute(Object object, Object attribute, Object value, JexlNode node) {
        // attempt to reuse last executor cached in volatile JexlNode.value
        if (node != null && cache) {
            Object cached = node.jjtGetValue();
            if (cached instanceof JexlPropertySet) {
                JexlPropertySet setter = (JexlPropertySet) cached;
                Object eval = setter.tryInvoke(object, attribute, value);
                if (!setter.tryFailed(eval)) {
                    return;
                }
            }
        }
        JexlException xjexl = null;
        JexlPropertySet vs = uberspect.getPropertySet(object, attribute, value, node);
        if (vs != null) {
            try {
                // cache executor in volatile JexlNode.value
                vs.invoke(object, value);
                if (node != null && cache && vs.isCacheable()) {
                    node.jjtSetValue(vs);
                }
                return;
            } catch (RuntimeException xrt) {
                if (node == null) {
                    throw xrt;
                }
                xjexl = new JexlException(node, "set object property error", xrt);
            } catch (Exception xany) {
                if (node == null) {
                    throw new RuntimeException(xany);
                }
                xjexl = new JexlException(node, "set object property error", xany);
            }
        }
        if (xjexl == null) {
            String error = "unable to set object property"
                           + ", class: " + object.getClass().getName()
                           + ", property: " + attribute;
            if (node == null) {
                throw new UnsupportedOperationException(error);
            }
            xjexl = new JexlException(node, error, null);
        }
        if (strict) {
            throw xjexl;
        }
        if (!silent) {
            logger.warn(xjexl.getMessage());
        }
    }

    /**
     * Unused, satisfy ParserVisitor interface.
     * @param node a node
     * @param data the data
     * @return does not return
     */
    public Object visit(SimpleNode node, Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unused, should throw in Parser.
     * @param node a node
     * @param data the data
     * @return does not return
     */
    public Object visit(ASTAmbiguous node, Object data) {
        throw new UnsupportedOperationException("unexpected type of node");
    }
}
