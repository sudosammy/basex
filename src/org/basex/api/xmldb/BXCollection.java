package org.basex.api.xmldb;

import java.io.IOException;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.XMLResource;
import org.basex.build.DiskBuilder;
import org.basex.build.xml.XMLParser;
import org.basex.core.Context;
import org.basex.core.proc.Open;
import org.basex.core.proc.Check;
import org.basex.core.proc.Close;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.io.IO;

/**
 * Implementation of the Collection Interface for the XMLDB:API
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXCollection implements Collection {
  /** Context reference. */
  private Context ctx;

  /**
   * Standard constructor.
   * @param ctx for Context
   */
  public BXCollection(Context ctx) {
    this.ctx = ctx;
  }

  /**
   * @see org.xmldb.api.base.Collection#close()
   */
  public void close() {
  new Close().execute(ctx);
  }

  /**
   * @see org.xmldb.api.base.Collection#createId()
   */
  public String createId() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#createResource(java.lang.String, java.lang.String)
   */
  public Resource createResource(String id, String type) throws XMLDBException {
    if(type.equals(XMLResource.RESOURCE_TYPE.toString())) {
      if(new Check(id).execute(ctx)) {
        return new BXXMLResource(ctx.current());
      }
    }
    throw new XMLDBException(ErrorCodes.NOT_IMPLEMENTED);
  }

  /**
   * @see org.xmldb.api.base.Collection#getChildCollection(java.lang.String)
   */
  public Collection getChildCollection(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#getChildCollectionCount()
   */
  public int getChildCollectionCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @see org.xmldb.api.base.Collection#getName()
   */
  public String getName() {
    return ctx.data().meta.dbname;
  }

  /**
   * @see org.xmldb.api.base.Collection#getParentCollection()
   */
  public Collection getParentCollection() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Configurable#getProperty(java.lang.String)
   */
  public String getProperty(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#getResource(java.lang.String)
   */
  public Resource getResource(String id) throws XMLDBException {
    if(new Open(id).execute(ctx)) return new BXXMLResource(ctx.current());
    throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
  }

  /**
   * @see org.xmldb.api.base.Collection#getResourceCount()
   */
  public int getResourceCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @see org.xmldb.api.base.Collection#getService(java.lang.String, java.lang.String)
   */
  public Service getService(String name, String version) throws XMLDBException {
    if(name.equals("XPathQueryService")) return new BXXPathQueryService(ctx);
    throw new XMLDBException(ErrorCodes.NO_SUCH_SERVICE);
  }

  /**
   * @see org.xmldb.api.base.Collection#getServices()
   */
  public Service[] getServices() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#isOpen()
   */
  public boolean isOpen() {
    if (new Check(ctx.data().meta.dbname).execute(ctx)) {
      return true;
    }
    return false;
  }

  /**
   * @see org.xmldb.api.base.Collection#listChildCollections()
   */
  public String[] listChildCollections() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#listResources()
   */
  public String[] listResources() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#removeResource(org.xmldb.api.base.Resource)
   */
  public void removeResource(Resource res) throws XMLDBException {
    if(new Check(res.getId()).execute(ctx)) {
      new DropDB(res.getId()).execute(ctx);
    } else {
      throw new XMLDBException(ErrorCodes.NO_SUCH_RESOURCE);
    }
  }

  /**
   * @see org.xmldb.api.base.Configurable#setProperty(java.lang.String, java.lang.String)
   */
  public void setProperty(String name, String value) {
  // TODO Auto-generated method stub

  }

  /**
   * @see org.xmldb.api.base.Collection#storeResource(org.xmldb.api.base.Resource)
   */
  public void storeResource(Resource res) throws XMLDBException {
    String cont = res.getContent().toString();
    try {
      /*
      Context ctx = new Context();
      new CreateDB(cont, "tmp").execute(ctx);
      ctx.data().insert(ctx.data().size, -1, ctx.data());
      ctx.data().flush();
      new DropDB("tmp").execute(ctx);
      */
      
      Data tmp = new DiskBuilder().build(new XMLParser(new IO(cont)), "tmp");
      ctx.data().insert(ctx.data().size, -1, tmp);
      ctx.data().flush();
      DropDB.drop("tmp");
      
    } catch(final IOException ex) {
      throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
    }
  }
}
