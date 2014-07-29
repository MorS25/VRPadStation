package com.cellbots;


import java.util.Arrays;

import com.laser.app.VrPadStationApp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * The Class PulseGenerator.
 */
public class PulseGenerator implements Runnable
{

  /** The sample rate. 44100hz is native on g1 */
  private int        sampleRate;

  /** The MI n_ pulse_ width. */
//  public int         MIN_PULSE_WIDTH;

  /** The MA x_ pulse_ width. */
//  public int         MAX_PULSE_WIDTH;

  /** The left channel pulse width. */
  private int        pulseWidthArray[];

  /** The pulse interval. */
  private int        pulseInterval;

  /** The noise audio track. */
  private AudioTrack noiseAudioTrack;

  /** The buffer length. */
  private int        bufferlength;

  /** The audio buffer. */
  private short[]    audioBuffer;

  private int audioChannel = 0;
  
  private boolean stop = false;
  private boolean pauseAudio = false;

  private static String TAG = "Pulse Generator";
  
  //double[] fpos = {0,0};
  private double fpos = 0;
  //short lastWave = 0;

  private VrPadStationApp app;
      
  /**
   * Instantiates a new pulse generator.
   */
  @SuppressWarnings("deprecation")
  public PulseGenerator(VrPadStationApp app)
  {
	this.app = app;  
	
    sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
    
    int numCh = app.channelManager.getChannelsArray().length;
    pulseWidthArray = new int[numCh];

    Arrays.fill(pulseWidthArray, 0);//( MIN_PULSE_WIDTH + MAX_PULSE_WIDTH ) / 2);

    //bufferlength = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
    
	float pulseFreq = app.settings.TRANSMISSION_RATE; //1.0f / 0.02f;  // 20ms = 50Hz
	if (app.settings.TRANSMISSION_RATE < 50)	// se è più basso crasha per dimensione buffer non valida
		pulseFreq = 50;
    pulseInterval = (int) ((float) (sampleRate) / pulseFreq); 
    
    // NO! Crea un ritardo.
    // ricalcolo il bufferlenght in modo che sia un multiplo di pulseInterval,
    // cos� da non lasciare spazio inutilizzato che mi pu� far sfasare l'onda
    //int diff = bufferlength % pulseInterval;
    //bufferlength = bufferlength - diff;
    
    // Come bufferlength utilizzo l'intervallo tra ogni impulso (20ms),
    // in questo modo non ho tempi di ritardo quando vario i canali
    bufferlength = pulseInterval*2; // * 2 perch� stereo
    
    noiseAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferlength, AudioTrack.MODE_STREAM);

    sampleRate = noiseAudioTrack.getSampleRate();

    Log.i(TAG, "BufferLength = " + Integer.toString(bufferlength));
    Log.i(TAG, "Sample Rate = " + Integer.toString(sampleRate));

    audioBuffer = new short[bufferlength];

  }
 
  public void init(int[] chValArray)
  {	  
	  for (int i = 0; i < chValArray.length; i++)
	  {
		  pulseWidthArray[i] = chValArray[i];
	  }	  
	  
	  try
	  {
		  noiseAudioTrack.play();
	  } catch(IllegalStateException ex)
	  {
		  Log.d("PulseGenerator", "Error playing AudioTrack.");
	  }
  }
  
  public static float GAS_FREQUENCY = 50;

  public void generatePPM()
  {
	  int[] widthArray = new int[pulseWidthArray.length];
	  while (!stop) 
      { 	
		  if (!pauseAudio && app.settings.PPMSUM)
		  {
			  if (app.settings.SWITCH_PPMSUM_CHANNEL)
				  audioChannel = 1;
			  else
				  audioChannel = 0;			  
			  
		      int delayWidth = 30;
			  
			  for (int z = 0; z < pulseWidthArray.length; z++)
			  {		      
			      float percent = (float)( getPulseVal(z) - app.settings.MIN_PULSE_WIDTH ) / (float)( app.settings.MAX_PULSE_WIDTH - app.settings.MIN_PULSE_WIDTH );		      
			      float dutyCycle = (percent+1) / 20.0f; // %
			      int pulseWidth = (int)(dutyCycle * ((float)pulseInterval)); // about 1,5ms
			      widthArray[z] = pulseWidth;
			      	
			      /*Log.i(TAG, "pulseInterval = " + Integer.toString(pulseInterval));
			      Log.i(TAG, "delayWidth = " + Integer.toString(delayWidth));
			      Log.i(TAG, "pulseWidth = " + Integer.toString(pulseWidth));*/
			  }
			  
		      for (int i = 0; i < bufferlength; i++) 
		      { 
		    	  if (i%2 == audioChannel)
		    	  {
		    		  if (app.settings.AUDIO_SIGNAL)
		    		  {
				          int j = 0; 
			        	  //we have to modulate the signal a bit because the sound 
			        	  //card freaks out if it goes dc 
				          for (int z = 0; z < widthArray.length; z++)
						  {	 
				        	  int k = 0; 
				        	  
					          // delay che uso per distinguere gli impulsi (dovrebbe essere 360us)
					          while (k < delayWidth && i < bufferlength) 
					          { 
						    	  if (i%2 == audioChannel)
						    	  {
						        	  if (!app.settings.ReverseSignal)
						        		  audioBuffer[i] = (short) ( -30000 - ( ( i % 2 ) * 100 ) );
						        	  else
						        		  audioBuffer[i] = (short) ( +30000 + ( ( i % 2 ) * 100 ) );
						        	  i++; 
						        	  j++; 
						        	  k++;
						    	  }
					        	  else if (i<audioBuffer.length)
						    	  {
				        		  	  i++;	
					        	  }
					          }
				        	  // impulso
					          while (k < widthArray[z] && i < bufferlength) 
					          { 	 
					        	  if (i%2 == audioChannel)
						    	  {
						        	  if (!app.settings.ReverseSignal)      	  
						        		  audioBuffer[i] = (short) ( +30000 + ( ( i % 2 ) * 100 ) );
						        	  else
						        		  audioBuffer[i] = (short) ( -30000 - ( ( i % 2 ) * 100 ) );
						        	  i++; 
						        	  j++; 
						        	  k++;
						    	  }
					        	  else if (i<audioBuffer.length)
					        	  {
					        		  i++;
					        	  }
					          }
						  } 
			        	  int k = 0;
				          // delay che uso per distinguere gli impulsi (dovrebbe essere 360us)
				          while (k < delayWidth && i < bufferlength) 
				          { 
				        	  if (i%2 == audioChannel)
					    	  {
					        	  if (!app.settings.ReverseSignal)
					        		  audioBuffer[i] = (short) ( -30000 - ( ( i % 2 ) * 100 ) ); 
					        	  else
					        		  audioBuffer[i] = (short) ( +30000 + ( ( i % 2 ) * 100 ) );
					        	  i++; 
					        	  j++; 
					        	  k++;
					    	  }
				        	  else if (i<audioBuffer.length)
				        	  {
				        		  i++;
				        	  }
				          }
				          
				          while (j < pulseInterval && i < bufferlength) 
				          { 
				        	  if (i%2 == audioChannel)
					    	  {
					        	  if (!app.settings.ReverseSignal)
					        		  audioBuffer[i] = (short) ( +30000 + ( ( i % 2 ) * 100 ) ); 
					        	  else
					        		  audioBuffer[i] = (short) ( -30000 - ( ( i % 2 ) * 100 ) );
					        	  i++; 
					        	  j++; 
					    	  }
				        	  else if (i<audioBuffer.length)
				        	  {
				        		  i++;
				        	  }
				          } 		
				      }
		    		  else
		    		  {
		    			  audioBuffer[i] = 0;
		    		  }
		    	  }
		      } 
		      
	    	  // Gestisco il secondo canale
	    	  playNote((audioChannel == 0 ? 1 : 0), GAS_FREQUENCY);
	    	  
	    	  //playNote((audioChannel == 0 ? 0 : 1), GAS_FREQUENCY);
	    	  
	    	  // Scrivo il buffer nella traccia audio
	    	  // try/catch perch� ogni tanto va in IllegalStateException: unable to retrieve audiotrack
	    	  // pointer for write(), alla chiusura, e non so perch�.
	    	  try
	    	  {
	    		  noiseAudioTrack.write(audioBuffer, 0, bufferlength);
	    	  }catch (IllegalStateException ex){}
		  }
      }
  }
  
  // Onda sinusoidale
  private double waveForm(double x)
  {
	  return Math.sin(2 * Math.PI * x);
  }
  
  // Onda quadra
  /*private double waveSquare(double x)
  {
	  return ((x < 0.5) ? 1.0 : -1.0);
  }*/
  
  private void playNote(int channel, float freq)
  {
	  
	  //numero campioni per singola forma d'onda
	  int waveSamples = (int) ((float) (sampleRate) / freq);
	  int tempPos = 0;
	  double tempFpos = 0;
	  //short tempWave = 0;
	  
	  //int numWaves = (audioBuffer.length / 2) / waveSamples;
	  //waveSamples = (audioBuffer.length / 2) / numWaves;
	  
	  for (int k = 0; k < audioBuffer.length / 2; k++ )
	  {
		  int i = channel + 2 * k;
		  if (app.settings.AUDIO_FEEDBACK)
		  {
			  //tempPos = pos + k % waveSamples;
			  tempPos = k % waveSamples;
			  //tempFpos = fpos[channel] + (double) tempPos / (double) waveSamples;
			  tempFpos = fpos + (double) tempPos / (double) waveSamples;
			  if (!app.settings.ReverseSignal)
				  audioBuffer[i] = (short) ( -30000.0  * waveForm(tempFpos));
			  else
				  audioBuffer[i] = (short) ( +30000.0  * waveForm(tempFpos));
		  
			 /*if (k < waveSamples)
			  {
				  audioBuffer[i] = (short) ( fpos * (double) audioBuffer[i] + (1.0 - fpos) * (double)lastWave);
			  }
			  tempWave = audioBuffer[i];*/
		  }
		  else
		  {
			  audioBuffer[i] = 0;
		  }
	  }	  
	  //fpos[channel] = tempFpos;
	  fpos = tempFpos;
	  //lastWave = tempWave;
  }

  public void run()
  {	  
	  generatePPM();
  }

  /**
   * Stop.
   */
  public void stop()
  {
    stop = true;
	noiseAudioTrack.pause();
	noiseAudioTrack.flush();
    noiseAudioTrack.stop();
    noiseAudioTrack.release();
  }
  
  /**
   * Loop continues but no sound is generated.
   * @param pause
   */
  public void pauseAudio(boolean pause)
  {
	  pauseAudio = pause;
  }

  public void setPulseValues(int[] valuesArray)
  {
	  for (int i = 0; i < pulseWidthArray.length; i++)
	  {
		  setPulseVal(valuesArray[i], i);
	  }
  }
  
  /**
   * Set the channel val
   * @param val
   */
  public /*synchronized*/ void setPulseVal(int val, int i)
  {
	  pulseWidthArray[i] = val;
  }
  /**
   * 
   * @return the channel val
   */
  public /*synchronized*/ int getPulseVal(int i)
  {
	  return pulseWidthArray[i];
  }

  /**
   * Gets the pulse ms.
   * 
   * @return the pulse ms
   */
  public float getPulseMs(int i)
  {
    return ( (float) pulseWidthArray[i] / sampleRate ) * 1000;
  }

  /**
   * Gets the pulse samples.
   * 
   * @return the pulse samples
   */
  public int getPulseSamples(int i)
  {
    return pulseWidthArray[i];
  }

//  public void updateSettings(LaserSettings settings) {
//	  this.settings = settings;
//  }

}