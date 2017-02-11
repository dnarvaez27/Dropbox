package com.dnarvaez27.dropbox;

import java.io.File;

/**
 * Interfaz necesaria para la notificación de eventos de la clase {@link Dropbox}<br>
 * Los eventos de Dropbox se generan cuando se termina de descargar o cargar un archivo. Resulta de mayor utilidad cuando hay flujo de directorios
 * 
 * @author d.narvaez11
 */
public interface DropboxEventListener
{
	/**
	 * Se invoca cuando ocurre un Evento de Dropbox de carga o descarga
	 * 
	 * @param file File cargado o descargado, generado por el evento
	 * @see
	 * 		{@link Dropbox#getSize(String)}<br>
	 *      {@link Dropbox#getSizeLocal(File)}<br>
	 *      {@link File#length()}<br>
	 *      <br>
	 *      <ul>
	 *      <b>Metodos generadores de Eventos de Dropbox</b>
	 *      <li>{@link Dropbox#download(String, String)}<br>
	 *      <li>{@link Dropbox#downloadFolder(String, String)}<br>
	 *      <li>{@link Dropbox#upload(File)}<br>
	 *      <li>{@link Dropbox#upload(File, boolean)}<br>
	 *      <li>{@link Dropbox#upload(File, boolean, String)}<br>
	 *      <li>{@link Dropbox#uploadFolder(File)}<br>
	 *      <li>{@link Dropbox#uploadFolder(File, boolean)}<br>
	 *      <li>{@link Dropbox#uploadFolder(File, boolean, String)}
	 *      </ul>
	 */
	public void onDataEvent( File file );
}