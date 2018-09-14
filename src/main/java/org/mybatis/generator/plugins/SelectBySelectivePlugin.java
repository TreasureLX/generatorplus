/**
 *    Copyright ${license.git.copyrightYears} the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.plugins;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * 根据条件筛选记录列表插件
 * @author Administrator
 *
 */
public class SelectBySelectivePlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}
	
	@Override
	public boolean clientGenerated(Interface interfaze,
			TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		interfaze.addMethod(genereateSelectBySelective(introspectedTable));
		return true;
	}
	
	
//	/**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method,
//            Interface interfaze, IntrospectedTable introspectedTable) {
//            interfaze.addMethod(genereateSelectBySelective(introspectedTable));
//        return true;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(
//            Method method, Interface interfaze,
//            IntrospectedTable introspectedTable) {
//
//            interfaze.addMethod(genereateSelectBySelective(introspectedTable));
//
//        return true;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method,
//            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
//        
//            topLevelClass.addMethod(genereateSelectBySelective(introspectedTable));
//        return true;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(
//            Method method, TopLevelClass topLevelClass,
//            IntrospectedTable introspectedTable) {
//        
//            topLevelClass.addMethod(genereateSelectBySelective(introspectedTable));
//        return true;
//    }
	
	@Override
	public boolean sqlMapDocumentGenerated(Document document,
			IntrospectedTable introspectedTable) {
		String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();//数据库表名 
		XmlElement parentElement = document.getRootElement();
		XmlElement answer = new XmlElement("select"); 
		answer.addAttribute(new Attribute("id", "selectBySelective"));
		answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
		FullyQualifiedJavaType parameterType = introspectedTable.getRules().calculateAllFieldsClass();
		answer.addAttribute(new Attribute("parameterType", parameterType.getFullyQualifiedName()));
		
		StringBuilder sb = new StringBuilder();
        sb.append("select ");
        
        answer.addElement(new TextElement(sb.toString()));
        answer.addElement(getBaseColumnListElement(introspectedTable));
        if (introspectedTable.hasBLOBColumns()) {
            answer.addElement(new TextElement(","));
            answer.addElement(getBlobColumnListElement(introspectedTable));
        }

        sb.setLength(0);
        sb.append("from ");
        sb.append(tableName);
        answer.addElement(new TextElement(sb.toString()));
        
        XmlElement dynamicElement = new XmlElement("where");
        answer.addElement(dynamicElement);
        
        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
        	XmlElement isNotNullElement = new XmlElement("if");
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");
            isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            dynamicElement.addElement(isNotNullElement);

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            sb.append(',');

            isNotNullElement.addElement(new TextElement(sb.toString()));
        }
//        answer.addElement(new TextElement("order by gmt_created desc"));
        
		parentElement.addElement(answer);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	/**
	 * 生成对应的java代码
	 * @param method
	 * @param introspectedTable
	 * @return
	 */
	private Method genereateSelectBySelective(IntrospectedTable introspectedTable){
		Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());

        Method method = new Method();
        method.setName("selectBySelective");
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType
                .getNewListInstance();
        FullyQualifiedJavaType listType;
        listType = new FullyQualifiedJavaType(
                introspectedTable.getBaseRecordType());

        importedTypes.add(listType);
        returnType.addTypeArgument(listType);
        method.setReturnType(returnType);
        
        
        FullyQualifiedJavaType parameterType = introspectedTable.getRules()
                .calculateAllFieldsClass();
        method.addParameter(new Parameter(parameterType, "record"));
        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        //topLevelClass.addMethod(method);
        return method;
	}
	
	protected XmlElement getBaseColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getBaseColumnListId()));
        return answer;
    }

    protected XmlElement getBlobColumnListElement(IntrospectedTable introspectedTable) {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getBlobColumnListId()));
        return answer;
    }
}
