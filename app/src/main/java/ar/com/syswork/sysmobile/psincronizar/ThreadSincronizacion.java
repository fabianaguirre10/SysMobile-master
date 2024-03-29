package ar.com.syswork.sysmobile.psincronizar;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;


import ar.com.syswork.sysmobile.entities.CuentaSession;
import ar.com.syswork.sysmobile.shared.AppSysMobile;
import ar.com.syswork.sysmobile.shared.HttpManager;

public class ThreadSincronizacion implements Runnable{

	private Handler h;
	private int tipoSincronizacion;
	private String rutaAcceso;
	private String  servidor;
	private String rutaAccesoServidor;
	final CuentaSession objcuentaSession= new CuentaSession();
	public ThreadSincronizacion(Handler h, int tipoSincronizacion, int pagina)
	{
		this.h = h;
		this.tipoSincronizacion = tipoSincronizacion; 
		
		switch (tipoSincronizacion)
		{
		case AppSysMobile.WS_VENDEDORES:
			rutaAcceso = AppSysMobile.WS_ACCESO_VENDEDORES;
			break;
		case AppSysMobile.WS_RUBROS:
			rutaAcceso = AppSysMobile.WS_ACCESO_RUBROS ;
			break;
		case AppSysMobile.WS_DEPOSITOS:
			rutaAcceso = AppSysMobile.WS_ACCESO_DEPOSITOS ;
			break;
		case AppSysMobile.WS_CLIENTES:
			rutaAcceso = AppSysMobile.WS_ACCESO_CLIENTES +"idAccount="+ objcuentaSession.getCu_idAccount()+"&Imeid="+AppSysMobile.WS_IMAIL;
			servidor="engine";

			break;
		case AppSysMobile.WS_ARTICULOS:
			rutaAcceso = AppSysMobile.WS_ACCESO_ARTICULOS + "/" + pagina;
			break;
		case AppSysMobile.WS_REGISTROS:
			rutaAcceso = AppSysMobile.WS_CONSULTA_CANTIDAD_REGISTOS;
			break;
			case AppSysMobile.WS_CUENTA:
				rutaAcceso = AppSysMobile.WS_CARGARCIENTAS;
				servidor="engine";
				break;
			case AppSysMobile.WS_DESAVENA:
				rutaAcceso =AppSysMobile.WS_CONSULTA_DESAVENA;
				break;
			case AppSysMobile.WS_DESFORMAPAGO:
				rutaAcceso =AppSysMobile.WS_CONSULTA_DESFORMAPAGO;
				break;
			case AppSysMobile.WS_DESVOLUMEN:
				rutaAcceso =AppSysMobile.WS_CONSULTA_DESVOLUMEN;
				break;
			case AppSysMobile.WS_DESPRECIOESCALA:
				rutaAcceso =AppSysMobile.WS_CONSULTA_DESPRECIOESCALA;
				break;
			case AppSysMobile.WS_CARTERA:
				rutaAcceso =AppSysMobile.WS_CONSULTA_CARTERA;
				break;
		}
	}

	@Override
	public void run() {
		
		String respuesta = "";
		int tipoRespuesta = 0;
		
		boolean intentar = true;
		int tiempo = 5000;
		int vez = 0;
		
		// intento una vez y si falla hago dos reintentos , pausando en el 1ro 5 segundos y en el 2do 10
		while (intentar)
		{
			
			if (vez>0){
				try 
				{
					Thread.sleep(tiempo * vez);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}

			if(servidor=="engine") {
				rutaAccesoServidor=rutaAcceso;
			}else{
				rutaAccesoServidor=AppSysMobile.getRutaWebService() + rutaAcceso;
			}

			HttpManager httpManager = new HttpManager(rutaAccesoServidor, AppSysMobile.getPuertoWebService());
			
			try {
				
				respuesta = httpManager.getStrDataByGET();
				tipoRespuesta = AppSysMobile.WS_RECIBE_DATOS;
				intentar = false;
				
			} catch (ClientProtocolException e) {
				
				respuesta  = "ClientProtocolException";
				tipoRespuesta = AppSysMobile.WS_RECIBE_ERRORES;
				
				e.printStackTrace();
				
			} catch (IOException e) {
				
				respuesta  = "IOException";
				tipoRespuesta = AppSysMobile.WS_RECIBE_ERRORES;
	
				e.printStackTrace();
			}
			
			vez++;
			if (vez==3)
			{
				intentar=false;
			}
		} 
		
		enviaMensaje (respuesta,tipoRespuesta);

	}
	
	private void enviaMensaje(String mensaje, int tipoRespuesta)
	{
		Message message = new Message();
		message.arg1 = tipoRespuesta;
		message.arg2 = tipoSincronizacion;
		message.obj = mensaje;
		h.sendMessage(message);
	}


}
