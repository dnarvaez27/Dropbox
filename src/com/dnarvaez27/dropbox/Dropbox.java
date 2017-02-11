package com.dnarvaez27.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;

/**
 * Clase que permite el flujo de archivos alojados en Dropbox<br>
 * Previamente se debe vincular la App a usar con Dropbox<br>
 * Se utiliza la API v2 de Dropbox.
 *
 * @author d.narvaez11
 * @see <a href="https://www.dropbox.com/developers/documentation/java">Dropbox API V2</a>
 * @see <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v2.0.x/">Dropbox API Javadoc</a>
 */
public class Dropbox
{
	/**
	 * AccessToken del usuario necesario para el flujo de archivos
	 */
	private String accessToken;

	/**
	 * Information of the App vinculada a Dropbox
	 */
	private DbxAppInfo appInfo;

	/**
	 * Cliente de Dropbox vinculado
	 */
	private DbxClientV2 client;

	/**
	 * Interface para la notificacion de eventos
	 */
	private DropboxEventListener dropboxEventListener;

	/**
	 * A grouping of a few configuration parameters for how we should make requests to the Dropbox servers.
	 */
	private DbxRequestConfig requestConfig;

	/**
	 * Does the OAuth 2 "authorization code
	 */
	private DbxWebAuth webAuth;

	/**
	 * Construye un objeto necesario para el vinculo con una cuenta Dropbox<br>
	 * Se construye con la llave y secreto de la aplicación previamente vinculada a Dropbox<br>
	 * <b>post:</b>
	 * <ul>
	 * <li>Se realizan las configuraciones necesarias de {@link DbxRequestConfig}
	 * <li>Se obtiene la información de la App por medio de {@link DbxAppInfo}
	 * <li>Se realiza la autenticación por medio de {@link DbxWebAuth}
	 * </ul>
	 *
	 * @param AppKey Key de la App vinculada a Dropbox
	 * @param AppSecret Secret de la App vinculada a Dropbox
	 */
	public Dropbox( String AppKey, String AppSecret )
	{
		requestConfig = new DbxRequestConfig( "dnarvaez27/fundacion_animal" );
		appInfo = new DbxAppInfo( AppKey, AppSecret );
		webAuth = new DbxWebAuth( requestConfig, appInfo );
	}

	/**
	 * Crea un nuevo folder en la carpeta de Dropbox con la ruta dada por parametro
	 *
	 * @param path Ruta del folder a crear
	 * @throws DbxException Si el path no concuerda con los patrones de ruta de Dropbox
	 * @throws CreateFolderErrorException Si hubo un error al crear el folder
	 * @see <a href ="http://dropbox.github.io/dropbox-sdk-java/api-docs/v2.0.x/">Dropbox API Javadoc </a>
	 */
	public void createFolder( String path ) throws CreateFolderErrorException, DbxException
	{
		client.files( ).createFolder( "/" + path );
	}

	/**
	 * Descarga un archivo de Dropbox en el destino especificado<br>
	 * Si se configuro {@link #setDropboxEvent(DropboxEventListener)}, se notifica
	 *
	 * @param fileToDownload Ruta del Archivo a descargar. Debe ser la ruta completa de la locación en Dropbox
	 * @param destino Ruta destino del archivo a descargar
	 * @return El archivo descargado, o null si el archivo no existe u ocurre una Excepción
	 * @throws IOException Si ocurre un problema con el FileInputStream
	 * @throws DbxException Si ocurre un problema al descargar el archivo
	 */
	public File download( String fileToDownload, String destino ) throws DbxException, IOException
	{
		File downloaded = new File( destino );
		FileOutputStream out = new FileOutputStream( downloaded );

		fileToDownload = fileToDownload.startsWith( "/" ) ? fileToDownload : "/" + fileToDownload;
		DownloadBuilder downloadBuilder = client.files( ).downloadBuilder( fileToDownload );
		downloadBuilder.download( out );

		if( dropboxEventListener != null )
		{
			dropboxEventListener.onDataEvent( downloaded );
		}
		return downloaded;
	}

	/**
	 * Descarga un directorio desde Dropbox en el destino especificado<br>
	 * Si se configuro {@link #setDropboxEvent(DropboxEventListener)}, se notifica
	 *
	 * @param folder Directorio de Dropbox a descargar
	 * @param destino Detino donde se descargará el directorio
	 * @throws IOException Si ocurre un error en el FileInputStream
	 * @throws DbxException Si ocurre un error en la descarga
	 * @see #download(String, String)
	 */
	public void downloadFolder( String folder, String destino ) throws DbxException, IOException
	{
		File fFolder = new File( destino );
		if( !fFolder.exists( ) )
		{
			fFolder.mkdirs( );
		}

		ListFolderResult result = client.files( ).listFolder( folder.isEmpty( ) ? "" : "/" + folder );
		for( Metadata metadata : result.getEntries( ) )
		{
			String name = metadata.getName( );
			File file = new File( destino + "/" + folder + "/" + name );
			if( !file.exists( ) )
			{
				if( metadata instanceof FileMetadata )
				{
					download( metadata.getPathLower( ).substring( 1 ), destino + "/" + metadata.getName( ) );
				}
				else
				{
					downloadFolder( metadata.getPathLower( ), destino + "/" + metadata.getName( ) );
				}
			}
		}
	}

	/**
	 * Retorna el AccessToken del usuario necesario para el flujo de archivos y consultas de la cuenta de Dropbox
	 *
	 * @return AccessToken del usuario
	 */
	public String getAcessToken( )
	{
		return accessToken;
	}

	/**
	 * Retorna el {@link DbxClientV2}, cliente de Dropbox
	 *
	 * @return Cliente de Dropbox
	 */
	public DbxClientV2 getClient( )
	{
		return client;
	}

	/**
	 * Retorna el {@link FullAccount}, cliente de Dropbox
	 *
	 * @return FullAccount del usuario dropbox
	 */
	public FullAccount getFullClient( )
	{
		if( client != null )
		{
			try
			{
				FullAccount account = client.users( ).getCurrentAccount( );
				return account;
			}
			catch( DbxException e )
			{
				e.printStackTrace( );
			}
		}
		return null;
	}

	/**
	 * Retorna el tamaño de un archivo o directorio en Dropbox
	 *
	 * @param file Ruta del archivo o directorio en Dropbox
	 * @return Tamaño del archivo o directorio en Dropbox
	 */
	public int getSize( String file )
	{
		int totalSize = 0;
		try
		{
			String toMet = file.isEmpty( ) || file.startsWith( "/" ) ? file : "/" + file;
			Metadata met = client.files( ).getMetadata( toMet );
			if( met instanceof FolderMetadata )
			{
				ListFolderResult result = client.files( ).listFolder( toMet );
				for( Metadata metadata : result.getEntries( ) )
				{
					if( metadata instanceof FileMetadata )
					{
						totalSize += ( ( FileMetadata ) metadata ).getSize( );
					}
					else if( metadata instanceof FolderMetadata )
					{
						totalSize += getSize( file + "/" + metadata.getName( ) );
					}
				}
			}
			else
			{
				totalSize += ( ( FileMetadata ) met ).getSize( );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace( );
		}
		return totalSize;
	}

	/**
	 * Verifica si esta conectado a Dropbox
	 *
	 * @return True si esta conectado con Dropbox, False de lo contrario
	 */
	public boolean isConnectedToDropbox( )
	{
		return client != null;
	}

	/**
	 * Retorna la fecha en la que el archivo cuya ruta es dada por parámetro fue modificado por última vez
	 *
	 * @param path Ruta del Archivo a consultar la fecha en que fue modificado
	 * @return Date con la información de la última fecha en la que el archivo con la ruta dada por parámetro fue modificado
	 * @throws Exception
	 */
	public Date lastModified( String path ) throws Exception
	{
		// try
		// {
		path = path.startsWith( "/" ) ? path : "/" + path;
		Metadata metadata = client.files( ).getMetadata( path );
		if( metadata instanceof FileMetadata )
		{
			return ( ( FileMetadata ) metadata ).getClientModified( );
		}
		else
		{
			return null;
		}
		// }
		// catch( Exception e )
		// {
		// e.printStackTrace( );
		// throw new Exception( e );
		// }
		// return null;
	}

	/**
	 * Realiza la vinculación de la cuenta del usuario con la App por medio de el código obtenido de {@link #linkDropbox()
	 *
	 * @param code Código de verificación para la vinculación obtenida por el URL de {@link #linkDropbox()}
	 */
	public DbxClientV2 link( String code )
	{
		code = code.trim( );

		try
		{
			DbxAuthFinish authFinish = webAuth.finishFromCode( code );

			accessToken = authFinish.getAccessToken( );

			client = new DbxClientV2( requestConfig, accessToken );

			return client;
		}
		catch( DbxException ex )
		{
			System.err.println( "Error in DbxWebAuth.authorize: " + ex.getMessage( ) );
			return null;
		}
	}

	/**
	 * Realiza una petición a Dropbox para la vinculación de la cuenta Dropbox del usuario con la App<br>
	 * Devuelve un String con el URL Web necesario para la obtención del codigo de verificación
	 *
	 * @return String con el URL Web necesario para la obtención del código de verificación.
	 */
	public String linkDropbox( )
	{
		DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder( ).withNoRedirect( ).build( );

		String url = webAuth.authorize( webAuthRequest );

		return url;
	}

	/**
	 * Lista todos los archivos de la carpeta de Dropboxa a la cual tiene acceso la App y cuya ruta se especifica por parametro
	 *
	 * @param folder Ruta del directorio de dropbox a listar
	 * @return Lista de Metadatos de los archivos de la carpeta de Dropboxa a la cual tiene acceso la App y cuya ruta se especifica por parametro
	 * @throws Exception Si el directorio no existe o no es un directorio
	 */
	public ArrayList<Metadata> listFiles( String folder ) throws Exception
	{
		ArrayList<Metadata> files = new ArrayList<>( );
		try
		{
			folder = folder.isEmpty( ) || folder.startsWith( "/" ) ? folder : "/" + folder;
			try
			{
				if( client.files( ).getMetadata( folder ) instanceof FileMetadata )
				{
					throw new Exception( "La ruta especificada no es un directorio de Dropbox" );
				}
			}
			catch( Exception e )
			{
				throw new Exception( "La ruta especificada no es un directorio de Dropbox" );
			}

			ListFolderResult result = client.files( ).listFolder( folder );
			for( Metadata metadata : result.getEntries( ) )
			{
				if( metadata instanceof FolderMetadata )
				{
					files.add( metadata );
					files.addAll( listFiles( metadata.getPathLower( ) ) );
				}
				else
				{
					files.add( metadata );
				}
			}
		}
		catch( DbxException e )
		{
			e.printStackTrace( );
		}
		return files;
	}

	/**
	 * Realiza la conexión con el AccessToken previamente obtenido
	 */
	public void reconnect( )
	{
		reconnect( accessToken );
	}

	/**
	 * Realiza la conexión con el AccessToken dado por parámetro
	 *
	 * @param pAccessToken AccessToken del usuario para hacer la reconexión
	 */
	public void reconnect( String pAccessToken )
	{
		setAcessToken( pAccessToken );
		client = new DbxClientV2( requestConfig, pAccessToken );
	}

	/**
	 * Establece el AccessToken del usuario
	 *
	 * @param acessToken AccessToken del usuario
	 */
	public void setAcessToken( String acessToken )
	{
		accessToken = acessToken;
	}

	/**
	 * Establece un listener para la escucha de eventos.<br>
	 * Los eventos de Dropbox se generan cuando se termina de descargar o cargar un archivo. Resulta de mayor utilidad cuando hay flujo de directorios
	 *
	 * @param dropboxEventListener DropboxEventListener Clase que implementa la interfáz {@link DropboxEventListener}, quien escuchará los eventos de Dropbox
	 */
	public void setDropboxEventListener( DropboxEventListener dropboxEventListener )
	{
		this.dropboxEventListener = dropboxEventListener;
	}

	/**
	 * Carga en la carpeta principal de Dropbox, el archivo dado por parámetro.<br>
	 * El archivo se carga con WriteMode.OVERWRITE.<br>
	 * Si se configuro {@link #setDropboxEvent(DropboxEventListener)}, se notifica
	 *
	 * @param file Archivo a cargar
	 * @see #upload(File, boolean)
	 */
	public void upload( File file )
	{
		upload( file, true );
	}

	/**
	 * Carga en la carpeta principal de Dropbox, el archivo dado por parámetro<br>
	 * Establece si se desea sobreescribir el archivo o agregarlo<br>
	 * Si se configuro {@link #setDropboxEvent(DropboxEventListener)}, se notifica
	 *
	 * @param file Archivo a cargar
	 * @param overwrite True para sobreescribir, False para agregarlo
	 * @see #upload(File, boolean, String)
	 */
	public void upload( File file, boolean overwrite )
	{
		upload( file, overwrite, "" );
	}

	/**
	 * Carga un archivo dado por parametro en un folder especifico de Dropbox<br>
	 * Establece si se desea sobreescribir el archivo o agregarlo<br>
	 * Si se configuro {@link #setDropboxEvent(DropboxEventListener)}, se notifica
	 *
	 * @param file Archivo a cargar
	 * @param overwrite True si sobreescribe el archivo, False para agregarlo
	 * @param folder Directorio donde se cargará el archivo
	 */
	public void upload( File file, boolean overwrite, String folder )
	{
		try( FileInputStream in = new FileInputStream( file ) )
		{
			UploadBuilder uploadBuilder = client.files( ).uploadBuilder( "/" + ( folder.isEmpty( ) ? "" : folder + "/" ) + file.getName( ) );
			uploadBuilder.withMode( overwrite ? WriteMode.OVERWRITE : WriteMode.ADD );
			uploadBuilder.uploadAndFinish( in );
			if( dropboxEventListener != null )
			{
				dropboxEventListener.onDataEvent( file );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace( );
		}
	}

	/**
	 * Carga un directorio en un directorio especifico de Dropbox.<br>
	 * El directorio se carga con WriteMode.OVERWRITE.<br>
	 * Si se configuro {@link #setDropboxEvent(DropboxEventListener)}, se notifica
	 *
	 * @param folder Directorio a cargar
	 * @see #uploadFolder(File, boolean, String)
	 */
	public void uploadFolder( File folder )
	{
		uploadFolder( folder, true );
	}

	/**
	 * Carga un directorio en la carpeta principal de Dropbox.<br>
	 * Establece si se desea sobreescribir el archivo o agregarlo<br>
	 * Si se configuro {@link #setDropboxEvent(DropboxEventListener)}, se notifica
	 *
	 * @param folder Directorio a cargar
	 * @param overwrite True para sobreescribir, False para agregar
	 * @see #uploadFolder(File, boolean, String)
	 */
	public void uploadFolder( File folder, boolean overwrite )
	{
		uploadFolder( folder, overwrite, "" );
	}

	/**
	 * Carga un directorio en un directorio parent especifico de Dropbox.<br>
	 * Establece si se desea sobreescribir el archivo o agregarlo<br>
	 * Si se configuro {@link #setDropboxEventListener(DropboxEventListener)}, se notifica
	 *
	 * @param folder Directorio a cargar
	 * @param overwrite True si sobreescribe el archivo, False para agregarlo
	 * @param dirParent Directorio que contendrá el directorio a agregar
	 */
	public void uploadFolder( File folder, boolean overwrite, String dirParent )
	{
		if( folder.isDirectory( ) )
		{
			String folderPath = ( dirParent.isEmpty( ) ? "" : dirParent + "/" ) + folder.getName( );
			folderPath = folderPath.replace( "\\", "/" );
			try
			{
				createFolder( ( folderPath.startsWith( "/" ) ? folderPath.substring( 1 ) : folderPath ) );
			}
			catch( CreateFolderErrorException e )
			{
				// Si el folder ya existe
				if( !overwrite )
				{
					e.printStackTrace( );
				}
			}
			catch( DbxException e )
			{
				e.printStackTrace( );
			}

			for( File archivo : folder.listFiles( ) )
			{
				if( archivo.isFile( ) )
				{
					upload( archivo, overwrite, folderPath );
				}
				else
				{
					uploadFolder( archivo, overwrite, folderPath/* + "/" + archivo.getName( ) */ );
				}
			}
		}
	}

	/**
	 * Retorna el tamaño total del File dado por parámetro.<br>
	 *
	 * @param file File del cual se conocerá el tamaño
	 * @return Tamaño del File dado por parámetro
	 */
	public static long getSizeLocal( File file )
	{
		long size = 0;
		if( file.isDirectory( ) )
		{
			for( File f : file.listFiles( ) )
			{
				size += getSizeLocal( f );
			}
		}
		else
		{
			size += file.length( );
		}
		return size;
	}
}
