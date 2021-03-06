package org.exfio.weave.account.exfiopeer.comm;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.exfio.weave.account.exfiopeer.comm.Client.EphemeralKey;
import org.exfio.weave.account.exfiopeer.comm.Message.EncodedMessage;
import org.exfio.weave.account.exfiopeer.comm.Message.MessageSession;
import org.exfio.weave.util.Log;
import org.exfio.weave.util.SQLUtils;

public class CommsStorage {

	public static final int QUERY_TIMEOUT = 10;
	
	public static void initDB(Connection db) throws SQLException {
		initDB(db, false);
	}
	
	public static void initDB(Connection db, boolean force) throws SQLException{
		PropertyDataMapper.initDB(db, force);
		ClientDataMapper.initDB(db, force);
		DefaultMessageDataMapper.initDB(db, force);		
	}

	public static String getProperty(Connection db, String key, String defaultValue) throws SQLException {
		try {
			return PropertyDataMapper.getProperty(db, key);
		} catch (StorageNotFoundException e) {
			return defaultValue;
		}
	}

	public static String getProperty(Connection db, String key) throws SQLException, StorageNotFoundException {
		return PropertyDataMapper.getProperty(db, key);
	}

	public static boolean hasProperty(Connection db, String key) throws SQLException {
		return PropertyDataMapper.hasProperty(db, key);
	}

	public static void setProperty(Connection db, String key, String value) throws SQLException {
		PropertyDataMapper.setProperty(db, key, value);
	}
	
	public static void deleteProperty(Connection db, String key) throws SQLException {
		PropertyDataMapper.deleteProperty(db, key);
	}

	public static Client getClient(Connection db, String clientId) throws SQLException, StorageNotFoundException {
		return ClientDataMapper.getClient(db, clientId);
	}

	public static Client getClientByName(Connection db, String clientName) throws SQLException, StorageNotFoundException {
		return ClientDataMapper.getClientByName(db, clientName);
	}

	public static Client getClientSelf(Connection db) throws SQLException, StorageNotFoundException {
		return ClientDataMapper.getClientSelf(db);
	}

	public static Client[] getClients(Connection db) throws SQLException {
		return ClientDataMapper.getClients(db);
	}

	public static void createClient(Connection db, Client client) throws SQLException {
		ClientDataMapper.createClient(db, client);
	}
	
	public static void updateClient(Connection db, Client client) throws SQLException {
		ClientDataMapper.updateClient(db, client);
	}
	
	public static void deleteClient(Connection db, String clientId) throws SQLException {
		ClientDataMapper.deleteClient(db, clientId);
	}

	public static EphemeralKey getEphemeralKey(Connection db, String clientId, String keyId) throws SQLException {
		return ClientDataMapper.getEphemeralKey(db, clientId, keyId, false);
	}

	public static void createEphemeralKey(Connection db, String clientId, EphemeralKey key) throws SQLException {
		ClientDataMapper.createEphemeralKey(db, clientId, key);
	}

	public static void updateEphemeralKey(Connection db, EphemeralKey key) throws SQLException {
		ClientDataMapper.updateEphemeralKey(db, key);
	}

	public static EphemeralKey[] getClientEphemeralKeys(Connection db, String clientId) throws SQLException {
		return ClientDataMapper.getClientEphemeralKeys(db, clientId, true, false);
	}

	public static void updateClientEphemeralKeys(Connection db, String clientId, List<EphemeralKey> keys) throws SQLException {
		ClientDataMapper.updateClientEphemeralKeys(db, clientId, keys.toArray(new EphemeralKey[0]));
	}

	public static void updateClientEphemeralKeys(Connection db, String clientId, EphemeralKey[] keys) throws SQLException {
		ClientDataMapper.updateClientEphemeralKeys(db, clientId, keys);
	}

	public static void deleteEphemeralKey(Connection db, String keyId) throws SQLException {
		ClientDataMapper.deleteEphemeralKey(db, keyId);
	}

	public static Message getMessage(Connection db, int messageId) throws SQLException {
		return DefaultMessageDataMapper.getMessage(db, messageId);
	}

	public static Message[] getMessages(Connection db) throws SQLException {
		return getMessages(db, null, null, null, true, false);
	}
	
	public static Message[] getMessages(Connection db, String sessionId, String messageType, String state, boolean includeRead, boolean includeDeleted) throws SQLException {
		return DefaultMessageDataMapper.getMessages(db, sessionId, messageType, state, includeRead, includeDeleted);				
	}

	public static int createMessage(Connection db, Message msg) throws SQLException {
		return DefaultMessageDataMapper.createMessage(db, msg.getEncodedMessage());
	}

	public static void updateMessage(Connection db, int messageId, boolean isRead, boolean isDeleted) throws SQLException {
		DefaultMessageDataMapper.updateMessage(db, messageId, isRead, isDeleted);
	}

	public static void deleteMessage(Connection db, int messageId) throws SQLException {
		DefaultMessageDataMapper.deleteMessage(db, messageId);
	}
	
	public static MessageSession getMessageSession(Connection db, String sessionId) throws SQLException {
		return DefaultMessageDataMapper.getMessageSession(db, sessionId);
	}

	public static MessageSession[] getMessageSessions(Connection db, String clientId) throws SQLException {
		return DefaultMessageDataMapper.getMessageSessions(db, clientId);		
	}

	public static void createMessageSession(Connection db, MessageSession session) throws SQLException {
		DefaultMessageDataMapper.createMessageSession(db, session);		
	}

	public static void updateMessageSession(Connection db, String sessionId, String state, Long sequence, Long otherSequence) throws SQLException {
		DefaultMessageDataMapper.updateMessageSession(db, sessionId, state, sequence, otherSequence);
	}

    private static ResultSet getGeneratedKeys(Connection db) throws SQLException {
    	return db.createStatement().executeQuery("select last_insert_rowid();");
    }
    
	//-------------------------------------------
	// Data Mapper classes
	//-------------------------------------------

	public static class PropertyDataMapper {

		public static void initDB(Connection db) throws SQLException {
			initDB(db, false);
		}
		
		public static void initDB(Connection db, boolean force) throws SQLException {
		
			String SQL = null;
			
			Statement st = db.createStatement();
			st.setQueryTimeout(QUERY_TIMEOUT);
		    
			//Create Property table
		    if ( force ) st.executeUpdate("DROP TABLE IF EXISTS Property");
		    
		    SQL = "CREATE TABLE IF NOT EXISTS Property"
		    	+ "\n"
		     	+ "("
		     	+ " Key TEXT PRIMARY KEY NOT NULL"
		     	+ " ,Value TEXT NOT NULL"
				+ " ,ModifiedDate TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP"
		     	+ ")";
		    st.executeUpdate(SQL);

		}

		public static String getProperty(Connection db, String key) throws SQLException, StorageNotFoundException {
			
			String SQL = null;
			SQL = "SELECT Value FROM Property WHERE Key = ?";
					
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			int col = 1;
			st.setString(col++, key);
			
			ResultSet rs = st.executeQuery();
			if ( !rs.next() ) {
				throw new StorageNotFoundException(String.format("Value not found for key '%s'", key));
			}
			
			return rs.getString("Value");
		}

		public static boolean hasProperty(Connection db, String key) throws SQLException {
			
			String SQL = null;
			SQL = "SELECT Value FROM Property WHERE Key = ?";
					
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			int col = 1;
			st.setString(col++, key);
			
			ResultSet rs = st.executeQuery();

			return ( (rs != null) && rs.next() );
		}

		public static void setProperty(Connection db, String key, String value) throws SQLException {

			String SQL = null;
			
			SQL = "REPLACE INTO Property"
				+ "\n"
				+ "("
				+ " Key"
				+ " ,Value"
				+ " ,ModifiedDate"
				+ ")"
				+ "\n"
				+ "VALUES(?, ?, CURRENT_TIMESTAMP)";

			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);	
			
			int col = 1;
			st.setString(col++, key);
			st.setString(col++, value);
			
			st.executeUpdate();
		}

		public static void deleteProperty(Connection db, String key) throws SQLException {
			String SQL = null;
			
			SQL = "DELETE FROM Property WHERE Key = ?";

			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);	
			
			int col = 1;
			st.setString(col++, key);
			
			st.executeUpdate();
		}

	}
	
	
	public static class ClientDataMapper {
		
		public static void initDB(Connection db) throws SQLException {
			initDB(db, false);
		}
		
		public static void initDB(Connection db, boolean force) throws SQLException {
		
			String SQL = null;
			
			Statement st = db.createStatement();
			st.setQueryTimeout(QUERY_TIMEOUT);

			//Create Client table
			if ( force ) st.executeUpdate("DROP TABLE IF EXISTS Client");
			
			SQL = "CREATE TABLE IF NOT EXISTS Client"
				+ "\n"
				+ "("
				+ " ClientID TEXT PRIMARY KEY NOT NULL"
				+ " ,IsSelf INTEGER NOT NULL DEFAULT 0"
				+ " ,Name TEXT NOT NULL"
				+ " ,PublicKey TEXT NOT NULL"
				+ " ,PrivateKey TEXT"
				+ " ,Status TEXT NOT NULL"
				+ " ,AuthLevel TEXT NOT NULL"
				+ " ,Version TEXT NOT NULL"
				+ " ,ModifiedDate TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP"
				+ " ,IsDeleted INTEGER NOT NULL DEFAULT 0"
				+ ")";
		    st.executeUpdate(SQL);
		    
			//Create Property table
		    if ( force ) st.executeUpdate("DROP TABLE IF EXISTS Property");
		    
		    SQL = "CREATE TABLE IF NOT EXISTS Property"
		    	+ "\n"
		     	+ "("
		     	+ " Key TEXT PRIMARY KEY NOT NULL"
		     	+ " ,Value TEXT NOT NULL"
				+ " ,ModifiedDate TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP"
		     	+ ")";
		    st.executeUpdate(SQL);

			//Create Ephemeral Key table
		    if ( force ) st.executeUpdate("DROP TABLE IF EXISTS EphemeralKey");
		    
		    SQL = "CREATE TABLE IF NOT EXISTS EphemeralKey"
		    	+ "\n"
		     	+ "("
		     	+ " EphemeralKeyID TEXT PRIMARY KEY NOT NULL"
		     	+ " ,ClientID TEXT NOT NULL"
		     	+ " ,PublicKey TEXT NOT NULL"
		     	+ " ,PrivateKey TEXT"
		     	+ " ,Status TEXT NOT NULL"
				+ " ,ModifiedDate TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP"
				+ " ,IsDeleted INTEGER NOT NULL DEFAULT 0"
		     	+ ")";
		    st.executeUpdate(SQL);

		}
		
		protected static String buildQueryGetClientById(String clientId, boolean includeDeleted) {
			String SQL = buildQueryGetClient(includeDeleted);
			
			SQL += " AND ClientID = " + SQLUtils.quote(clientId);
			
			return SQL;
		}
		
		protected static String buildQueryGetClient(boolean includeDeleted) {
			
			String SQL = null;
			
			SQL = "SELECT"
				+ " ClientID"		
				+ " ,IsSelf"
				+ " ,Name"
				+ " ,PublicKey"
				+ " ,PrivateKey"
				+ " ,Status"
				+ " ,AuthLevel"
				+ " ,Version"
				+ " ,ModifiedDate"
				+ "\n"
				+ "FROM"
				+ " Client"
				+ "\n"
				+ "WHERE";

			if ( includeDeleted ) {
				SQL += " 1=1";
			} else {
				SQL += " NOT IsDeleted"; 
			}
			
			return SQL;
		}

		protected static Client buildClientFromDB(Connection db, ResultSet rs) throws SQLException {
			
			Client client = new Client();
			client.setClientId(rs.getString("ClientID"));
			client.setSelf(rs.getBoolean("IsSelf"));
			client.setClientName(rs.getString("Name"));
			client.setPublicKey(rs.getString("PublicKey"));
			client.setPrivateKey(rs.getString("PrivateKey"));
			client.setStatus(rs.getString("Status"));
			client.setAuthLevel(rs.getString("AuthLevel"));
			client.setVersion(rs.getString("Version"));
			client.setModifiedDate(SQLUtils.sqliteDatetime(rs.getString("ModifiedDate")));
			
			EphemeralKey[] ekeys = getClientEphemeralKeys(db, client.getClientId(), true, false);
			client.setEphemeralKeys(new ArrayList<EphemeralKey>(Arrays.asList(ekeys)));
	
			return client;
		}
		
		public static Client getClient(Connection db, String clientId) throws SQLException, StorageNotFoundException {
			
			String SQL = buildQueryGetClientById(clientId, false);
					
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			ResultSet rs = st.executeQuery();
			if ( !rs.next() ) {
				throw new StorageNotFoundException(String.format("Client not found with ClientID '%s'", clientId));
			}
			
			Client client = buildClientFromDB(db, rs);

			return client;
		}

		public static Client getClientByName(Connection db, String clientName) throws SQLException, StorageNotFoundException {
			
			String SQL = buildQueryGetClient(false);
			
			SQL += " AND ClientName = ?";
			
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			int col = 1;
			st.setString(col++, clientName);
			
			ResultSet rs = st.executeQuery();
			if ( !rs.next() ) {
				throw new StorageNotFoundException(String.format("Client not found with ClientName '%s'", clientName));
			}
			
			Client client = buildClientFromDB(db, rs);

			return client;
		}

		public static Client getClientSelf(Connection db) throws SQLException, StorageNotFoundException {
			
			String SQL = buildQueryGetClient(false);
			
			SQL += " AND IsSelf = 1";
			
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			ResultSet rs = st.executeQuery();
			if ( !rs.next() ) {
				throw new StorageNotFoundException(String.format("Client not found with IsSelf '1'"));
			}
			
			Client client = buildClientFromDB(db, rs);

			return client;
		}

		public static Client[] getClients(Connection db) throws SQLException {
			
			List<Client> clients = new LinkedList<Client>();
			
			String SQL = buildQueryGetClient(false);
					
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			ResultSet rs = st.executeQuery();
			while ( rs.next() ) {
				Client client = buildClientFromDB(db, rs);
				clients.add(client);
			}
			
			return clients.toArray(new Client[0]);
		}

		public static void createClient(Connection db, Client client) throws SQLException {
			
			String SQL = null;
			
	        //Create client record
			SQL = "INSERT INTO Client"
				+ "\n"
				+ "("
				+ " ClientID"
				+ " ,IsSelf"
				+ " ,Name"
				+ " ,PublicKey"
				+ " ,PrivateKey"
				+ " ,Status"
				+ " ,AuthLevel"
				+ " ,Version"
				+ " ,ModifiedDate"				
				+ ")"
				+ "\n"
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);		

			int col = 1;
			st.setString(col++, client.getClientId());
			st.setInt   (col++, client.isSelf() ? 1 : 0);
			st.setString(col++, client.getClientName());
			st.setString(col++, client.getPublicKey());
			st.setString(col++, client.getPrivateKey());
			st.setString(col++, client.getStatus());
			st.setString(col++, client.getAuthLevel());
			st.setString(col++, client.getVersion());
			
			st.executeUpdate();
			
			updateClientEphemeralKeys(db, client.getClientId(), client.getEphemeralKeys());
		}

		public static void updateClient(Connection db, Client client) throws SQLException {
			
			String SQL = null;
			
	        //Update client record
			SQL = "UPDATE Client"
				+ "\n"
				+ "SET"
				+ " IsSelf        = ?"
				+ " ,Name         = ?"
				+ " ,PublicKey    = ?"
				+ " ,PrivateKey   = ?"
				+ " ,Status       = ?"
				+ " ,AuthLevel    = ?"
				+ " ,Version      = ?"
				+ " ,ModifiedDate = CURRENT_TIMESTAMP"
				+ "\n"
				+ "WHERE"
				+ " ClientID = ?";
			
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);		

			int col = 1;
			st.setInt   (col++, client.isSelf() ? 1 : 0);
			st.setString(col++, client.getClientName());
			st.setString(col++, client.getPublicKey());
			st.setString(col++, client.getPrivateKey());
			st.setString(col++, client.getStatus());
			st.setString(col++, client.getAuthLevel());
			st.setString(col++, client.getVersion());
			st.setString(col++, client.getClientId());
			
			st.executeUpdate();
			
			updateClientEphemeralKeys(db, client.getClientId(), client.getEphemeralKeys());
		}

		public static void deleteClient(Connection db, String clientId) throws SQLException {
			
	        //Logically delete client record
			String SQL = "UPDATE Client"
					   + "\n"
					   + "SET"
					   + " ModifiedDate = CURRENT_TIMESTAMP"
					   + " IsDeleted    = 1"
					   + "\n"
					   + "WHERE"
					   + " ClientID = ?";

			PreparedStatement pst = db.prepareStatement(SQL);
			pst.setQueryTimeout(QUERY_TIMEOUT);		
			int col = 1;
			pst.setString(col++, clientId);
			
			pst.executeUpdate();
		}

		public static String buildQueryGetEphemeralKeyById(String clientId, String keyId, boolean includeDeleted) {
			String SQL = buildQueryGetEphemeralKey(includeDeleted);
			
			SQL += " AND ClientID = " + SQLUtils.quote(clientId);
			SQL += " AND EphemeralKeyID = " + SQLUtils.quote(keyId);
			
			return SQL;
		}

		public static String buildQueryGetEphemeralKeyByClient(String clientId, boolean isPublished, boolean includeDeleted) {
			String SQL = buildQueryGetEphemeralKey(includeDeleted);
			
			SQL += " AND ClientID = " + SQLUtils.quote(clientId);

			if ( isPublished ) {
				SQL += " AND Status = " + SQLUtils.quote("published");
			}
			
			return SQL;
		}

		public static String buildQueryGetEphemeralKey(boolean includeDeleted) {
			
			String SQL = null;
			
			SQL = "SELECT"
				+ " EphemeralKeyID"
				+ " ,ClientID"
				+ " ,PublicKey"
				+ " ,PrivateKey"
				+ " ,Status"
				+ " ,ModifiedDate"
				+ "\n"
				+ "FROM"
				+ " EphemeralKey"
				+ "\n"
				+ "WHERE ";

			if ( includeDeleted ) {
				SQL += " 1=1";
			} else {
				SQL += " NOT IsDeleted"; 
			}

	     	return SQL;
		}
		
		public static EphemeralKey getEphemeralKey(Connection db, String clientId, String keyId, boolean includeDeleted) throws SQLException {
			
			String SQL = buildQueryGetEphemeralKeyById(clientId, keyId, includeDeleted);
					
			PreparedStatement pst = db.prepareStatement(SQL);
			pst.setQueryTimeout(QUERY_TIMEOUT);
			
			ResultSet rs = pst.executeQuery();
			if ( !rs.next() ) {
				return null;
			}
			
			EphemeralKey key = new EphemeralKey();
			key.setKeyId       (rs.getString("EphemeralKeyID"));
			key.setPublicKey   (rs.getString("PublicKey"));
			key.setPrivateKey  (rs.getString("PrivateKey"));
			key.setStatus      (rs.getString("Status"));
			key.setModifiedDate(SQLUtils.sqliteDatetime(rs.getString("ModifiedDate")));
			
			return key;
		}

		public static EphemeralKey[] getClientEphemeralKeys(Connection db, String clientId, boolean isPublished, boolean includeDeleted) throws SQLException {

			String SQL = buildQueryGetEphemeralKeyByClient(clientId, isPublished, includeDeleted);
			
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);

			List<EphemeralKey> keys = new LinkedList<EphemeralKey>();
			
			ResultSet rs = st.executeQuery();
			while ( rs.next() ) {
			
				EphemeralKey key = new EphemeralKey();
				key.setKeyId       (rs.getString("EphemeralKeyID"));
				key.setPublicKey   (rs.getString("PublicKey"));
				key.setPrivateKey  (rs.getString("PrivateKey"));
				key.setStatus      (rs.getString("Status"));
				key.setModifiedDate(SQLUtils.sqliteDatetime(rs.getString("ModifiedDate")));
			
				keys.add(key);
			}
			
			return keys.toArray(new EphemeralKey[0]);
		}

		public static void updateClientEphemeralKeys(Connection db, String clientId, List<EphemeralKey> keys) throws SQLException {
			updateClientEphemeralKeys(db, clientId, keys.toArray(new EphemeralKey[0]));
		}
		public static void updateClientEphemeralKeys(Connection db, String clientId, EphemeralKey[] keys) throws SQLException {
			updateClientEphemeralKeys(db, clientId, keys, false);
		}
		public static void updateClientEphemeralKeys(Connection db, String clientId, EphemeralKey[] keys, boolean delete) throws SQLException {

			if ( delete ) {
				//Delete keys that have been provisioned or revoked

				//Build dictionary of keys by id
				Map<String, EphemeralKey> mapKeys = new HashMap<String, EphemeralKey>();
				for ( int i = 0; i < keys.length; i++ ) {
					mapKeys.put(keys[i].getKeyId(), keys[i]);
				}

				List<String> delKeys = new LinkedList<String>();
	
				String SQL = buildQueryGetEphemeralKeyByClient(clientId, true, false);
				
				PreparedStatement st = db.prepareStatement(SQL);
				st.setQueryTimeout(QUERY_TIMEOUT);
	
				//If key is not in dictionary assume it has been provisioned or revoked
				ResultSet rs = st.executeQuery();
				while ( rs.next() ) {
					if ( !mapKeys.containsKey(rs.getString("EphemeralKeyID")) ) {
						delKeys.add(rs.getString("EphemeralKeyID"));
					}
				}
	
				if ( delKeys.size() > 0 ) {
					
					SQL = "UPDATE EphemeralKey"
						+ "\n"
						+ "SET"
						+ " ModifiedDate = CURRENT_TIMESTAMP"
						+ " ,IsDeleted   = 1"
						+ "\n"
						+ "WHERE ";
					
					SQL += String.format(" EphemeralKeyID IN (%s)", SQLUtils.quoteArray(delKeys.toArray(new String[0])));
	
					PreparedStatement delSt = db.prepareStatement(SQL);
					delSt.setQueryTimeout(QUERY_TIMEOUT);	
					delSt.executeUpdate();
				}
			}

			//Update keys
			for ( int i = 0; i < keys.length; i++ ) {
				if ( getEphemeralKey(db, clientId, keys[i].getKeyId(), true) == null ) {
					createEphemeralKey(db, clientId, keys[i]);		
				} else {
					updateEphemeralKey(db, keys[i]);
				}
			}
			
		}

		public static void createEphemeralKey(Connection db, String clientId, EphemeralKey key) throws SQLException {

			String SQL = null;
			
			SQL = "INSERT INTO EphemeralKey"
				+ "\n"
				+ "("
				+ " EphemeralKeyID"
				+ " ,ClientID"
				+ " ,PublicKey"
				+ " ,PrivateKey"
				+ " ,Status"
				+ " ,ModifiedDate"
				+ ")"
				+ "\n"
				+ "VALUES(?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);	
			
			int col = 1;
			st.setString(col++, key.getKeyId());
			st.setString(col++, clientId);
			st.setString(col++, key.getPublicKey());		
			st.setString(col++, key.getPrivateKey());
			st.setString(col++, key.getStatus());
			
			st.executeUpdate();
		}

		public static void updateEphemeralKey(Connection db, EphemeralKey key) throws SQLException {

			String SQL = null;
			
			SQL = "UPDATE EphemeralKey"
				+ "\n"
				+ "SET"
				+ " PublicKey     = ?"
				+ " ,PrivateKey   = ?"
				+ " ,Status       = ?"
				+ " ,ModifiedDate = CURRENT_TIMESTAMP"
				+ "\n"
				+ "WHERE"
				+ " EphemeralKeyID = ?";

			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);	
			
			int col = 1;
			st.setString(col++, key.getPublicKey());		
			st.setString(col++, key.getPrivateKey());
			st.setString(col++, key.getStatus());
			st.setString(col++, key.getKeyId());
			
			st.executeUpdate();
		}

		public static void deleteEphemeralKey(Connection db, String keyId) throws SQLException {
			String SQL = null;
			
			SQL = "UPDATE EphemeralKey"
				+ "\n"
				+ "SET"
				+ " ModifiedDate = CURRENT_TIMESTAMP"
				+ " ,IsDeleted   = 1"
				+ "\n"
				+ "WHERE"
				+ " EphemeralKeyID = ?";

			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);	
			
			int col = 1;
			st.setString(col++, keyId);
			
			st.executeUpdate();
		}

	}

	public static class DefaultMessageDataMapper {
		
		public static void initDB(Connection db) throws SQLException {
			initDB(db, false);
		}
		
		public static void initDB(Connection db, boolean force) throws SQLException {
	
			String SQL = null;
	
			Statement st = db.createStatement();
			st.setQueryTimeout(QUERY_TIMEOUT);
	
		    if ( force ) st.executeUpdate("DROP TABLE IF EXISTS MessageSession");
		    
		    SQL = "CREATE TABLE IF NOT EXISTS MessageSession"
		    	+ "\n"
		    	+ "("
		    	+ " MessageSessionID TEXT PRIMARY KEY NOT NULL"
		    	+ " ,EphemeralKeyID TEXT NOT NULL"
		    	+ " ,Sequence INTEGER NOT NULL default 0"  	
		    	+ " ,OtherClientID TEXT NOT NULL"
		    	+ " ,OtherIdentityKey TEXT NOT NULL"   	
		    	+ " ,OtherEphemeralKeyID TEXT NOT NULL"
		    	+ " ,OtherEphemeralKey TEXT NOT NULL"
		    	+ " ,OtherSequence INTEGER NOT NULL default 0"
		    	+ " ,State TEXT NOT NULL"
		    	+ ")";
		    st.executeUpdate(SQL);
			
		    
		    if ( force ) st.executeUpdate("DROP TABLE IF EXISTS Message");
		    
		    SQL = "CREATE TABLE IF NOT EXISTS Message"
		    	+ "\n"
		    	+ "("
		    	+ " MessageID INTEGER PRIMARY KEY NOT NULL"
		    	+ " ,MessageSessionID TEXT NOT NULL"
		    	+ " ,SourceClientID TEXT NOT NULL"
		    	+ " ,SourceEphemeralKeyID TEXT NOT NULL"
		    	+ " ,DestinationClientID TEXT NOT NULL"
		    	+ " ,DestinationEphemeralKeyID TEXT NOT NULL"
		    	+ " ,Version TEXT NOT NULL"
		    	+ " ,Sequence INTEGER NOT NULL"
		    	+ " ,MessageType TEXT NOT NULL"
		    	+ " ,Content TEXT NOT NULL"
		    	+ " ,ModifiedDate TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP"
		    	+ " ,IsRead INTEGER NOT NULL DEFAULT 0"
		    	+ " ,IsDeleted INTEGER NOT NULL DEFAULT 0"
		    	+ ")";
		    st.executeUpdate(SQL);
	
		}
	
		protected static String buildQueryGetMessageById(int messageId, boolean includeDeleted) {
			String SQL = buildQueryGetMessage(true, includeDeleted);
			
			SQL += " AND m.MessageID = " + SQLUtils.quote(messageId);
			
			return SQL;
		}

		/*
		protected static String buildQueryGetMessageBySessionId(String sessionId, boolean includeRead, boolean includeDeleted) {
			String SQL = buildQueryGetMessage(includeRead, includeDeleted);
			
			SQL += " AND ms.MessageSessionID = " + SQLUtils.quote(sessionId);
			
			return SQL;
		}
		*/
		
		protected static String buildQueryGetMessage(boolean includeRead, boolean includeDeleted) {

			String SQL = null;
			
			//Get messages and session info for client
			SQL = "SELECT"
				+ " ms.MessageSessionID"
				+ " ,ms.EphemeralKeyID"
				+ " ,ms.OtherClientID"
				+ " ,ms.OtherIdentityKey"
				+ " ,ms.OtherEphemeralKeyID"
				+ " ,ms.OtherEphemeralKey"
				+ " ,ms.State"
			    + " ,m.MessageID"
				+ " ,m.SourceClientID"
		    	+ " ,m.SourceEphemeralKeyID"
		    	+ " ,m.DestinationClientID"
		    	+ " ,m.DestinationEphemeralKeyID"
		    	+ " ,m.Version"	    	
		    	+ " ,m.Sequence"
		    	+ " ,m.MessageType"
		    	+ " ,m.Content"		
		    	+ " ,m.ModifiedDate"
		    	+ "\n"		
				+ "FROM"
				+ " Message m"
				+ " JOIN MessageSession ms ON ms.MessageSessionID = m.MessageSessionID"
				+ "\n"
				+ "WHERE";
	
			if ( includeDeleted ) {
				SQL += " 1=1"; 				
			} else {
				SQL += " NOT IsDeleted"; 
			}

			if ( !includeRead ) {
				SQL += " AND NOT IsRead";
			}

			return SQL;
		}

		protected static String buildQueryGetMessageSessionById(String sessionId) {

			String SQL = buildQueryGetMessageSession(true);
			
			SQL += " AND ms.MessageSessionID = " + SQLUtils.quote(sessionId);
			
			return SQL;
		}

		protected static String buildQueryGetMessageSessionByClient(String clientId) {

			String SQL = buildQueryGetMessageSession(false);
			
			SQL += " AND ms.OtherClientID = " + SQLUtils.quote(clientId);
			
			return SQL;
		}

		protected static String buildQueryGetMessageSession(boolean includeClosed) {

			String SQL = null;
			
			//Get message session
			SQL = "SELECT"
				+ " ms.MessageSessionID"
				+ " ,ms.EphemeralKeyID"
				+ " ,ms.OtherClientID"
				+ " ,ms.OtherIdentityKey"
				+ " ,ms.OtherEphemeralKeyID"
				+ " ,ms.OtherEphemeralKey"
				+ " ,ms.State"
				+ "\n"
				+ "FROM"
				+ " MessageSession ms"
				+ "\n"
				+ "WHERE";
			
			if ( includeClosed ) {
				SQL += " 1=1"; 				
			} else {
				SQL += " NOT IsClosed"; 
			}

			return SQL;
		}

		protected static Message buildMessage(Connection db, ResultSet rs) throws SQLException {
			return buildMessage(db, rs, new EncodedMessage());
		}
		
		protected static Message buildMessage(Connection db, ResultSet rs, EncodedMessage msg) throws SQLException {
	
			msg.setMessageId(rs.getInt("MessageID"));
			msg.setSourceClientId(rs.getString("SourceClientID"));
			msg.setSourceKeyId(rs.getString("SourceEphemeralKeyID"));
			msg.setDestinationClientId(rs.getString("DestinationClientID"));
			msg.setDestinationKeyId(rs.getString("DestinationEphemeralKeyID"));
			msg.setVersion(rs.getString("Version"));
			msg.setSequence(rs.getLong("Sequence"));
			msg.setMessageType(rs.getString("MessageType"));
			msg.setContent(rs.getString("Content"));		
			msg.setModifiedDate(SQLUtils.sqliteDatetime(rs.getString("ModifiedDate")));

			
			//build session object
			msg.setSession(buildMessageSession(rs));
	
			return msg;
		}
	
		public static Message getMessage(Connection db, int messageId) throws SQLException {
			
			String SQL = null;
			
			//Get messages and session info for client
			SQL = DefaultMessageDataMapper.buildQueryGetMessageById(messageId, false);
			
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			ResultSet rs = st.executeQuery();

			if ( !rs.next() ) {
				throw new SQLException(String.format("Couldn't load message for MessageID '%d'", messageId));
			}
			
			return buildMessage(db, rs);
		}

		public static Message[] getMessages(Connection db) throws SQLException {
			return getMessages(db, null, null, null, true, false);
		}
		
		public static Message[] getMessages(Connection db, String sessionId, String messageType, String state, boolean includeRead, boolean includeDeleted) throws SQLException {
					
			String SQL = DefaultMessageDataMapper.buildQueryGetMessage(includeRead, includeDeleted);			

			//Build optional where clause parameters
			String sqlWhere = "";
			List<String> sqlParams = new ArrayList<String>();
			
			if ( sessionId != null ) {
				sqlWhere += " AND ms.MessageSessionID = ?" + "\n";
				sqlParams.add(sessionId);
			}
			
			if ( messageType != null ) {
				sqlWhere += " AND m.MessageType = ?" + "\n";
				sqlParams.add(messageType);
			}

			if ( state != null ) {
				sqlWhere += " AND ms.State = ?" + "\n";
				sqlParams.add(state);
			}
			
			SQL += sqlWhere;
			
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			for (int col = 0; col < sqlParams.size(); col++) {
				st.setString(col+1, sqlParams.get(col));
			}
			ResultSet rs = st.executeQuery();
			
			List<Message> messages = new LinkedList<Message>();

			while ( rs.next() ) {
				messages.add(buildMessage(db, rs));
			}
			
			return messages.toArray(new Message[0]);
		}

		public static int createMessage(Connection db, EncodedMessage msg) throws SQLException {
			
			//Create session if it does not already exist
			if ( getMessageSession(db, msg.getMessageSessionId()) == null ) {
				createMessageSession(db, msg.getSession());
			}
			
			String SQL = null;
				
		    //Create message record
			SQL = "INSERT INTO Message"
				+ "\n"
				+ "("
				+ " MessageSessionID"
				+ " ,SourceClientID"
		    	+ " ,SourceEphemeralKeyID"
		    	+ " ,DestinationClientID"
		    	+ " ,DestinationEphemeralKeyID"
				+ " ,Version"	    	
				+ " ,Sequence"
				+ " ,MessageType"
				+ " ,Content"
				+ " ,ModifiedDate"		
				+ ")"
				+ "\n"
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
	
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);		
			int col = 1;
			st.setString(col++, msg.getMessageSessionId());
			st.setString(col++, msg.getSourceClientId());
			st.setString(col++, msg.getSourceKeyId());		
			st.setString(col++, msg.getDestinationClientId());
			st.setString(col++, msg.getDestinationKeyId());		
			st.setString(col++, msg.getVersion());
			st.setLong(col++, msg.getSequence());
			st.setString(col++, msg.getMessageType());
			st.setString(col++, msg.getContent());
	
			st.executeUpdate();
			
			ResultSet generatedKeys = getGeneratedKeys(db);
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
			} else {
				throw new SQLException("Creating message failed, no ID obtained.");
			}
		}
	
		public static void updateMessage(Connection db, int msgId, boolean isRead, boolean isDeleted) throws SQLException {
			
			String SQL = null;
			
	        //Logically delete message record
			SQL = "UPDATE Message"
				+ "\n"
				+ "SET"
				+ " ModifiedDate = CURRENT_TIMESTAMP"
				+ " ,IsRead      = ?"
				+ " ,IsDeleted   = ?"
				+ "\n"
				+ "WHERE"
				+ " MessageID = ?";
	
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);		
			int col = 1;
			st.setInt(col++, isRead ? 1 : 0);			
			st.setInt(col++, isDeleted ? 1 : 0);			
			st.setInt(col++, msgId);
			
			st.executeUpdate();
		}

		public static void deleteMessage(Connection db, int msgId) throws SQLException {
			
			String SQL = null;
			
	        //Logically delete message record
			SQL = "UPDATE Message"
				+ "\n"
				+ "SET"
				+ " ModifiedDate = CURRENT_TIMESTAMP"
				+ " ,IsDeleted   = 1"
				+ "\n"
				+ "WHERE"
				+ " MessageID = ?";
	
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);		
			int col = 1;
			st.setInt(col++, msgId);
			
			st.executeUpdate();
		}
		
		protected static MessageSession buildMessageSession(ResultSet rs) throws SQLException {
			MessageSession sess = new MessageSession();
			sess.setSessionId(rs.getString("MessageSessionID"));
			sess.setEphemeralKeyId(rs.getString("EphemeralKeyID"));
			sess.setOtherClientId(rs.getString("OtherClientID"));
			sess.setOtherIdentityKey(rs.getString("OtherIdentityKey"));
			sess.setOtherEphemeralKeyId(rs.getString("OtherEphemeralKeyID"));
			sess.setOtherEphemeralKey(rs.getString("OtherEphemeralKey"));
			sess.setState(rs.getString("State"));
			
			return sess;
		}

		public static MessageSession getMessageSession(Connection db, String sessionId) throws SQLException {
			
			String SQL = buildQueryGetMessageSessionById(sessionId);
					
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			ResultSet rs = st.executeQuery();
			if ( !rs.next() ) {
				return null;
			}
			
			return buildMessageSession(rs);
		}

		public static MessageSession[] getMessageSessions(Connection db, String clientId) throws SQLException {
			
			String SQL = buildQueryGetMessageSessionByClient(clientId);
					
			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);
			
			List<MessageSession> sessions = new LinkedList<MessageSession>();

			ResultSet rs = st.executeQuery();
			while ( rs.next() ) {
				sessions.add(buildMessageSession(rs));
			}
			
			return sessions.toArray(new MessageSession[0]);
		}

		public static void createMessageSession(Connection db, MessageSession session) throws SQLException {
			
			String SQL = null;
			
		    //Create session record
			SQL = "INSERT INTO MessageSession"
				+ "\n"
				+ "("
				+ " MessageSessionID"
				+ " ,EphemeralKeyID"
				+ " ,Sequence"
				+ " ,OtherClientID"
				+ " ,OtherIdentityKey"
				+ " ,OtherEphemeralKeyID"
				+ " ,OtherEphemeralKey"
				+ " ,OtherSequence"
				+ " ,State"				
				+ ")"
				+ "\n"
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);		
			int col = 1;
			st.setString(col++, session.getSessionId());
			st.setString(col++, session.getEphemeralKeyId());
			st.setLong(col++, session.getSequence());
			st.setString(col++, session.getOtherClientId());
			st.setString(col++, session.getOtherIdentityKey());
			st.setString(col++, session.getOtherEphemeralKeyId());
			st.setString(col++, session.getOtherEphemeralKey());
			st.setLong(col++, session.getOtherSequence());
			st.setString(col++, session.getState());

			st.executeUpdate();
		}

		public static void updateMessageSession(Connection db, String sessionId, String state, Long sequence, Long otherSequence) throws SQLException {
			
			if (state == null && sequence == null && otherSequence == null) {
				Log.getInstance().warn("DefaultMessangerDataMapper.updateMessageSession() called with null arguments");
				return;
			}
			
			String setClause = "";
			if ( state != null ) {
				setClause += (setClause.length() == 0 ? " " : " ,") + "State = " + SQLUtils.quote(state);
			}
			if ( sequence != null ) {
				setClause += (setClause.length() == 0 ? " " : " ,") + "Sequence = " + sequence.longValue();
			}
			if ( otherSequence != null ) {
				setClause += (setClause.length() == 0 ? " " : " ,") + "OtherSequence = " + otherSequence.longValue();
			}
			
			String SQL = null;
			
		    //Create session record
			SQL = "UPDATE MessageSession"
				+ "\n"
				+ "SET"
				+ "\n" + setClause + "\n"
				+ "WHERE"
				+ " MessageSessionID = ?";

			PreparedStatement st = db.prepareStatement(SQL);
			st.setQueryTimeout(QUERY_TIMEOUT);		
			int col = 1;			
			st.setString(col++, sessionId);

			st.executeUpdate();
		}

	}
}
