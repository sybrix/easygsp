package com.sybrix.easygsp.transformations;


import java.lang.reflect.Modifier
import java.util.logging.Level
import java.util.logging.Logger
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.runtime.metaclass.ThreadManagedMetaBeanProperty

/**
 * EasyGSPTransformation <br/>
 *
 * @author David Lee
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class EntityTransformation implements ASTTransformation {
        private static final Logger logger = Logger.getLogger(EntityTransformation.class.getName())

        public void visit(ASTNode[] astNodes, SourceUnit source) {
                if (!astNodes) return

                try {
                        if (!(astNodes[0] instanceof ModuleNode)) {
                                return;
                        }

                        List<ClassNode> classes = (astNodes[0] as ModuleNode).classes;

                        if (classes.size() > 0) {
                                if (getFullName(classes.get(0)).startsWith('models.')) {
                                       // injectIdProperty(classes.get(0))
                                        logger.finer("modifying model: " + getFullName(classes.get(0)))
                                        changeParent(classes.get(0))
                                        //addSetProperty(classes.get(0))
                                        addStaticDAOMethods("delete", getFullName(classes.get(0)), classes.get(0))
                                        addStaticDAOMethods("findAll", getFullName(classes.get(0)), classes.get(0))
                                        addStaticDAOMethods("find", getFullName(classes.get(0)), classes.get(0))
                                        addStaticDAOMethods("list", getFullName(classes.get(0)), classes.get(0))

                                        //addDelete(getFullName(classes.get(0)), classes.get(0))
                                }
                        }
//                                        //injectPkListProperty(classes.get(0))


                } catch (Throwable e) {
                        e.printStackTrace();
                }
        }

        private def addStaticDAOMethods(String methodName, String className, ClassNode classNode) {
                def statement = new AstBuilder().buildFromString(
                        "return com.sybrix.easygsp.db.Model." + methodName + "(\"" + className + "\", paramsMap)"
                )[0]

                classNode.addMethod(new MethodNode(methodName, Modifier.PUBLIC | Modifier.STATIC,
                        ClassHelper.OBJECT_TYPE,
                        [new Parameter(ClassHelper.OBJECT_TYPE,
                                'paramsMap')] as Parameter[],
                        [] as ClassNode[],
                        statement)
                )
        }


        private def addDelete(String className, ClassNode classNode) {
                def statement = new AstBuilder().buildFromString(
                        "return this.delete()"
                )[0]

                classNode.addMethod(new MethodNode("delete", Modifier.PUBLIC,
                        ClassHelper.OBJECT_TYPE,
                        [] as Parameter[],
                        [] as ClassNode[],
                        statement)
                )
        }


        public void changeParent(ClassNode classNode) {

                classNode.superClass = new ClassNode(com.sybrix.easygsp.db.Model.class)

                //parent.superClass = new ClassNode(easyom.Model.class)
//                Class cls = Class.forName(getFullName(classNode))
//                Field[] fields = cls.declaredFields
//                for (int i = 0; i < fields.length; i++) {
//                        if (fields[i].getAnnotation(easyom.Id.class) != null) {
//                                Field f = cls.getField('primaryKeys')
//                                List keys = f.get(null)
//                                keys.add(fields[i].name)
//                                 println("adding " + fields[i].name + " to " + getFullName(classNode))
//                        }
//                }

        }

        public void injectIdProperty(ClassNode classNode) {
                //final boolean hasId = GrailsASTUtils.hasOrInheritsProperty(classNode, GrailsDomainClassProperty.IDENTITY);

                //if (!hasId) {
                // inject into furthest relative
                ClassNode parent = getFurthestUnresolvedParent(classNode);
                logger.log(Level.FINER, "Injecting 'dynamicProperties' property into " + getFullName(classNode))

                parent.addProperty('dynamicProperties', Modifier.PRIVATE, new ClassNode([].class), new ListExpression(), null, null);
                //}
        }

        public static ClassNode getFurthestUnresolvedParent(ClassNode classNode) {
                ClassNode parent = classNode.getSuperClass();

                while (parent != null && !getFullName(parent).equals("java.lang.Object") && !parent.isResolved() && !Modifier.isAbstract(parent.getModifiers())) {
                        classNode = parent;
                        parent = parent.getSuperClass();
                }

                return classNode;
        }

        public static String getFullName(ClassNode classNode) {
                return classNode.getName();
        }
}




