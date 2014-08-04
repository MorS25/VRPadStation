package com.laser.ui.widgets;

import java.util.List;
import java.util.Vector;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class MyPagerAdapter extends FragmentPagerAdapter {

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			if (s_singleInstance == null)
				s_singleInstance = this;
		return super.instantiateItem(container, position);
		}

		public interface PagerAdapterListener
		{
			void OnFragmentChanged(int position);
		}
		   
		protected static MyPagerAdapter s_singleInstance = null;
		public static MyPagerAdapter GetSingletone()
		{
			return s_singleInstance;
		}
	   
	    PagerAdapterListener m_Listener = null;
	    private Vector<PagerAdapterListener> listListener = new Vector<PagerAdapterListener>();
	
		public void RegisterListener(PagerAdapterListener pal)
		{
			listListener.add(pal);
		}
		
		public void UnRegisterListener(PagerAdapterListener pal)
		{
			listListener.remove(pal);
		}
		
		
	   private Object fragment = null;
	
	   @Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			super.setPrimaryItem(container, position, object);
			if (listListener != null)
			{
				if ( fragment == null || fragment != object)
				{
					fragment = object;
					/*for (int c = 0; c < listListener.size(); c++)
					{
						listListener.get(c).OnFragmentChanged(position);
					}*/
					setCurrItem(position);
				}
			}
		}
	   
	   
	   public void setCurrItem(int position)
	   {
		   if (listListener != null)
			{
				for (int c = 0; c < listListener.size(); c++)
				{
					listListener.get(c).OnFragmentChanged(position);
				}
			}
	   }

	   // fragments to instantiate in the viewpager
	   private final List<Fragment> fragments;
	   private final List<String> fragmentTitles;
	   
	   // constructor
	   public MyPagerAdapter(FragmentManager fm,List<Fragment> fragments, List<String> fragmentTitles) {
	      super(fm);
	      this.fragments = fragments;
	      this.fragmentTitles = fragmentTitles;
	   }
	   
	   // return access to fragment from position, required override
	   @Override
	   public Fragment getItem(int position) {
	      return this.fragments.get(position);
	   }

	// number of fragments in list, required override
	   @Override
	   public int getCount() {
	      return this.fragments.size();
	   }
	   
	   @Override
       public CharSequence getPageTitle(int position) {

               if (null == fragmentTitles || fragmentTitles.size() <= position) {
                       return "unknown";
               }
               String fragTitle = fragmentTitles.get(position);
               if (null == fragTitle || fragTitle.trim().length() == 0) {
                       return "No Title";
               }
               // Log.v("TitleAdapter - getPageTitle=", fragmentTitles.get(position));
               return fragmentTitles.get(position);
       }

}