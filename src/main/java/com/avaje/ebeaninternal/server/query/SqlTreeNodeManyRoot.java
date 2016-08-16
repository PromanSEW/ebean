package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;

import java.sql.SQLException;
import java.util.List;

public final class SqlTreeNodeManyRoot extends SqlTreeNodeBean {

  private final BeanPropertyAssocMany<?> manyProp;

  public SqlTreeNodeManyRoot(String prefix, BeanPropertyAssocMany<?> prop, SqlTreeProperties props, List<SqlTreeNode> myList, boolean disableLazyLoad) {
    super(prefix, prop, props, myList, disableLazyLoad);
    this.manyProp = prop;
  }

  @Override
  public EntityBean load(DbReadContext cquery, EntityBean parentBean, EntityBean contextParent) throws SQLException {
    // pass in null for parentBean because the localBean
    // that is built is added to a collection rather than
    // being set to the parentBean directly
    EntityBean detailBean = super.load(cquery, null, null);
    // initialise the collection and add detailBean if it is not null
    if (contextParent != null) {
      manyProp.addBeanToCollectionWithCreate(contextParent, detailBean, false);
    }
    return detailBean;
  }

  /**
   * Force outer join for everything after the many property.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    super.appendFrom(ctx, joinType.autoToOuter());
  }

  @Override
  public boolean hasMany() {
    return true;
  }
}
