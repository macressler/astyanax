package com.netflix.astyanax.cql.reads;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import com.datastax.driver.core.Query;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.astyanax.CassandraOperationType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.cql.CqlAbstractExecutionImpl;
import com.netflix.astyanax.cql.util.ChainedContext;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.query.ColumnQuery;

public class CqlColumnQueryImpl<C> implements ColumnQuery<C> {

	private ChainedContext context; 
	
	CqlColumnQueryImpl(ChainedContext ctx) {
		this.context = ctx;
	}
	
	@Override
	public OperationResult<Column<C>> execute() throws ConnectionException {
		return new InternalColumnQueryExecutionImpl().execute();
	}

	@Override
	public ListenableFuture<OperationResult<Column<C>>> executeAsync() throws ConnectionException {
		return new InternalColumnQueryExecutionImpl().executeAsync();
	}

	private class InternalColumnQueryExecutionImpl extends CqlAbstractExecutionImpl<Column<C>> {

		public InternalColumnQueryExecutionImpl() {
			super(context);
		}

		@Override
		public CassandraOperationType getOperationType() {
			return CassandraOperationType.GET_COLUMN;
		}

		@Override
		public Query getQuery() {
			String keyspace = context.getNext(String.class);
			ColumnFamily<?, ?> cf = context.getNext(ColumnFamily.class);
			Object rowKey = context.getNext(Object.class); 
			Object column = context.getNext(Object.class); 
			
			Query query = QueryBuilder.select(String.valueOf(column))
					  .from(keyspace, cf.getName())
					  .where(eq("key", rowKey));
			return query;
		}

		@Override
		public Column<C> parseResultSet(ResultSet rs) {
			Row row = rs.one();
			String columnName = rs.getColumnDefinitions().asList().get(0).getName();
			return new CqlColumnImpl<C>(columnName, row, 0);
		}
	}
}
