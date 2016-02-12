package com.magicmoremagic.jbsc.objects.queries;

import java.util.ArrayList;
import java.util.List;

import com.magicmoremagic.jbsc.objects.types.FieldRef;

public class AndExpression implements QueryExpression {

	private List<QueryExpression> subExpressions;
	private FieldList fields;
	
	public AndExpression() {
		subExpressions = new ArrayList<QueryExpression>();
	}
	
	public void addSubExpression(QueryExpression expression) {
		subExpressions.add(expression);
		fields = null;
	}
	
	@Override
	public FieldList getParameters() {
		if (fields == null) {
			fields = new FieldList();	
	
			for (QueryExpression expr : subExpressions) {
				for (FieldRef ref : expr.getParameters().get()) {
					// todo copy fieldrefs and adjust indices if necessary
				}
			}
		}
		return fields;
	}

	@Override
	public String getSQL() {
		if (subExpressions.size() > 1) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (QueryExpression expr : subExpressions) {
				if (first) first = false;
				else sb.append(" AND ");
				
				if (expr instanceof OrExpression) {
					sb.append('(');
					sb.append(expr.getSQL());
					sb.append(')');
				} else sb.append(expr.getSQL());
			}
			return sb.toString();
		} else if (!subExpressions.isEmpty()) {
			return subExpressions.get(0).getSQL();
		}
		
		return "";
	}

}
