package com.magicmoremagic.jbsc.objects.queries;

import java.util.Arrays;

import com.magicmoremagic.jbsc.objects.types.FieldRef;

public class FieldRefSqlExpression implements QueryExpression {

	private FieldList fields;
	private String sql;
	
	public FieldRefSqlExpression() {
		fields = new FieldList();
	}
	
	public void setFieldRef(FieldRef ref) {
		FieldRef copy = new FieldRef(ref.getType(), ref.getName(), 0);
		fields.set(Arrays.asList(copy));
	}
	
	public void setSQL(String sql) {
		this.sql = sql;
	}
	
	@Override
	public FieldList getParameters() {
		return fields;
	}

	@Override
	public String getSQL() {
		if (sql != null) return sql;
		
		// if sql not provided, check each column 
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int index : fields.getSqlIndices()) {
			if (first) first = false;
			else sb.append(" AND ");
			
			sb.append(fields.getColName(index));
			sb.append(" = ?");
		}
		return sb.toString();
	}

}
