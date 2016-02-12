package com.magicmoremagic.jbsc.objects.queries;

import com.magicmoremagic.jbsc.objects.types.FieldRef;
import com.magicmoremagic.jbsc.objects.types.FieldType;

public class SqlExpression implements QueryExpression {

	private static final FieldList EMPTY_FIELDS = new FieldList() {
		@Override
		public FieldList set(java.util.List<FieldRef> fields) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public FieldList add(FieldRef ref) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public FieldList add(FieldType type, String name) {
			throw new UnsupportedOperationException();
		}
	};
	
	private String sql;
	
	public SqlExpression(String sql) {
		this.sql = sql;
	}
	
	@Override
	public FieldList getParameters() {
		return EMPTY_FIELDS;
	}

	@Override
	public String getSQL() {
		return sql;
	}

}
