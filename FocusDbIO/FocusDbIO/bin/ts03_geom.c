/*
// This source code is proprietary property of Tricon Geophysics Inc.
// and may not be copied or used without permission.
//
// Verifies Tricon geometry interface.
//
//
//
//                             Written by Wayne P. Collier January 2005
//
*/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#include <tri_utilities.h>
#include <tri_memory.h>
#include <tri_array.h>
#include <tricon_geometry.h>

#ifdef BUILD_EPOS3
//#include "LockClient/LCInit.h"
#endif

static void print_message( const char * message )
{
  printf( "%s\n", message );
}

int main( int argc, char ** argv )
{
  char usage[]=
    "Testsuite: Verifies the Tricon SDB Database interface.            \n"
    "                                                                  \n"
    "usage: ts03_geom <single option> project line                     \n"
    "                                                                  \n"
    "   option                                                         \n"
    "                                                                  \n"
    "    -p   Print the Shot, Reciever and CDP information.            \n"
    "                                                                  \n"
    "    -c   Compute the cdp, iline and xline of xy coords           \n"
    "                                                                  \n";

  char *option;
  char project[16];
  char line[16];
  triconGeometry tg;

  long4 shot_station;
  long4 recv_station;
  struct geom_coord *recv, *shot;

  struct geom_coord new_shot, new_recv;
  long4 shot_num, recv_num;
  float adjust;

  long4 cdp, iline, xline;
  long4 calc_cdp, calc_iline, calc_xline;
  float calc_x, calc_y;
  float x_coord, y_coord;
  float save_x, save_y;

  double x_coord_d, y_coord_d;
  double save_x_d, save_y_d;



  if ( argc < 2 )
    {
      fprintf( stderr, "%s\n", usage );
      exit( 13 );
    }

  option = argv[1];

  trierr_register_debug_callback( print_message );
  trierr_register_log_callback( print_message );
  trierr_register_error_callback( print_message );
  trierr_register_fatal_callback( print_message );

  if ( argc < 4 )
    {
      strcpy( project, "scott3d" );
      strcpy( line,    "pauls3d"     );
    }
  else
    {
      strncpy( project, argv[2], 15 );
      strncpy( line,    argv[3], 15 );
    }

    #ifdef BUILD_EPOS3
    //InitLockClient ( argc, argv );
    #endif

  if ( strcmp( option, "-p" ) == 0  )
      {
	trimem_verify_on();
	tg = tricon_geometry_initialize( project, line );
	trigeom_print( tg );
	trimem_report_usage();
	tricon_geometry_destroy( tg );
	trimem_report_usage();
      }

  if ( strcmp( option, "-l" ) == 0 )
    {
      tg = tricon_geometry_initialize( project, line );

      for ( shot_station = tg->shot_first; shot_station <= tg->shot_last; shot_station++ )
	{
	  if ( (shot = trigeom_lookup_shot_coord( tg, shot_station )) != NULL )
	    {
	      printf( "Found shot %-8d at %8.2f %8.2f\n", shot->ident, shot->x_coord, shot->y_coord );
	    }
	  else
	    {
	      printf( "Shot Station not found %d\n", shot_station );
	    }
	}

      for ( recv_station = tg->receiver_first; recv_station <= tg->receiver_last; recv_station++ )
	{
	  if ( (recv = trigeom_lookup_recv_coord( tg, recv_station )) != NULL )
	    {
	      printf( "Found recv %-8d at %8.2f %8.2f\n", recv->ident, recv->x_coord, recv->y_coord );
	    }
	  else
	    {
	      printf( "Recv station not found. %d\n", recv_station );
	    }
	}

    }

  if ( strcmp( option, "-w" ) == 0 )
    {
      tg = tricon_geometry_initialize( project, line );

      for( shot_num = 1; shot_num <= tg->num_shots; shot_num++ )
	{
	  adjust = ((float)shot_num)/ 100;
	  shot = trigeom_lookup_shot_coord( tg, shot_num );
	  new_shot.ident = shot->ident;
	  new_shot.state = 90;
	  new_shot.x_coord = shot->x_coord + adjust;
	  new_shot.y_coord = shot->y_coord + adjust;
	  trigeom_modify_shot_coord( tg, &new_shot );
	}

      for( recv_num = 1; recv_num <= tg->num_receivers; recv_num++ )
	{
	  adjust = ((float)recv_num)/ 100;
	  recv = trigeom_lookup_recv_coord( tg, recv_num );
	  new_recv.ident = recv->ident;
	  new_recv.state = 90;
	  new_recv.x_coord = recv->x_coord + adjust;
	  new_recv.y_coord = recv->y_coord + adjust;
	  trigeom_modify_recv_coord( tg, &new_recv );
	}



      trigeom_save_modifications( tg );



      tricon_geometry_destroy( tg );
    }


  if ( strcmp( option, "-x" ) == 0 )
    {

      tg = tricon_geometry_initialize( project, line );

      for( shot_num = 1; shot_num <= tg->num_shots; shot_num++ )
	{
	  adjust = ((float)shot_num)/ 100;
	  shot = trigeom_lookup_shot_coord( tg, shot_num );
	  new_shot.ident = shot->ident;
	  new_shot.state = 90;
	  new_shot.x_coord = shot->x_coord - adjust;
	  new_shot.y_coord = shot->y_coord - adjust;
	  trigeom_modify_shot_coord( tg, &new_shot );
	}

      for( recv_num = 1; recv_num <= tg->num_receivers; recv_num++ )
	{
	  adjust = ((float)recv_num)/ 100;
	  recv = trigeom_lookup_recv_coord( tg, recv_num );
	  new_recv.ident = recv->ident;
	  new_recv.state = 90;
	  new_recv.x_coord = recv->x_coord - adjust;
	  new_recv.y_coord = recv->y_coord - adjust;
	  trigeom_modify_recv_coord( tg, &new_recv );
	}



      trigeom_save_modifications( tg );



      tricon_geometry_destroy( tg );
     }

  if ( strcmp( option, "-c" ) == 0 )
    {

      tg = tricon_geometry_initialize( project, line );

      printf( "\n\n\nChecking Center Points.........\n\n\n" );
      cdp = tg->cdp.first;
      for ( iline = 1; iline <= tg->cdp.num_inlines; iline++ )
	{
	  for ( xline = 1; xline <= tg->cdp.num_xlines; xline++ )
	    {
	      x_coord = tg->cdp.origin_x +
		(iline -1) * tg->cdp.xline_delta_x +
		(xline-1) * tg->cdp.inline_delta_x;
	      y_coord  = tg->cdp.origin_y +
		(iline -1) * tg->cdp.xline_delta_y +
		(xline -1)* tg->cdp.inline_delta_y;

	      if ( trigeom_get_geom_from_coord(  tg, x_coord, y_coord, &calc_cdp, &calc_iline, &calc_xline ) )
		{

		  if ( calc_cdp != cdp || iline != calc_iline || xline != calc_xline )

		    {
		      printf( "CDP %5d %5d  iline %3d %3d  Xline %3d %3d  Center %7.0f %7.0f\n",
			      cdp, calc_cdp, iline, calc_iline, xline, calc_xline,  x_coord, y_coord );
		    }
		}
	      else
		{
		  printf( "CDP Geom Failed: CDP %5d   iline %3d   Xline %3d   Center %7.0f %7.0f\n",
			  cdp, iline, xline,  x_coord, y_coord );
		}

	      cdp++;
	    }
	}




      tricon_geometry_destroy( tg );
    }

  if ( strcmp( option, "-d" ) == 0 )
    {

      tg = tricon_geometry_initialize( project, line );

      cdp = tg->cdp.first;
      x_coord_d = tg->cdp.origin_x;
      y_coord_d = tg->cdp.origin_y;
      save_x_d = x_coord_d;
      save_y_d = y_coord_d;
      for ( iline = 1; iline <= tg->cdp.num_inlines; iline++ )
	{
	  for ( xline = 1; xline <= tg->cdp.num_xlines; xline++ )
	    {
	      if ( trigeom_get_coord_from_line( tg, iline, xline, &calc_x, &calc_y, &calc_cdp ) )
		{
		  printf ( "%5d %10.2f %10.2f - %10.2lf %10.2lf - %10.6f %10.6f \n",
			   calc_cdp, calc_x, calc_y, x_coord_d, y_coord_d,
			   fabsf( calc_x - x_coord_d ), fabsf( calc_y - y_coord_d ) );

		}
	      else
		{
		  printf ( "%10.2f %10.2f Failed \n", x_coord, y_coord );
		}





	      x_coord_d += tg->cdp.inline_delta_x;
	      y_coord_d += tg->cdp.inline_delta_y;



	    }
	  x_coord_d = save_x_d + tg->cdp.xline_delta_x;
	  y_coord_d = save_y_d + tg->cdp.xline_delta_y;
	  save_x_d = x_coord_d;
	  save_y_d = y_coord_d;
	}
      tricon_geometry_destroy( tg );
    }


  if ( strcmp( option, "-e" ) == 0 )
    {

      tg = tricon_geometry_initialize( project, line );

      cdp = tg->cdp.first;
      x_coord_d = tg->cdp.origin_x;
      y_coord_d = tg->cdp.origin_y;
      save_x_d = x_coord_d;
      save_y_d = y_coord_d;
      for ( iline = 1; iline <= tg->cdp.num_inlines; iline++ )
	{
	  for ( xline = 1; xline <= tg->cdp.num_xlines; xline++ )
	    {
	      if ( trigeom_get_coord_from_cdp( tg, cdp, &calc_x, &calc_y, &calc_iline, &calc_xline ) )
		{
		  printf ( "%5d %10.2f %10.2f - %10.2lf %10.2lf - %10.6f %10.6f - %5d %5d - %5d %5d \n",
			   cdp, calc_x, calc_y, x_coord_d, y_coord_d,
			   fabsf( calc_x - x_coord_d ), fabsf( calc_y - y_coord_d ),
			   iline, xline, calc_iline, calc_xline );

		}
	      else
		{
		  printf ( "%5d Failed \n", cdp );
		}




	      cdp++;
	      x_coord_d += tg->cdp.inline_delta_x;
	      y_coord_d += tg->cdp.inline_delta_y;



	    }
	  x_coord_d = save_x_d + tg->cdp.xline_delta_x;
	  y_coord_d = save_y_d + tg->cdp.xline_delta_y;
	  save_x_d = x_coord_d;
	  save_y_d = y_coord_d;
	}
      tricon_geometry_destroy( tg );



    }



  if ( strcmp( option, "-f" ) == 0 )
    {
      struct cdp_grid *grid;

      tg = tricon_geometry_initialize_cdp_grid( project, line );

      grid = &tg->cdp;

      printf( "CDP Grid:\n"  );

      printf( "    First CDP   %8d\n", grid->first);
      printf( "    Last CDP    %8d\n", grid->last);
      printf( "    Num Xlines  %8d\n", grid->num_xlines);
      printf( "    Num ilines %8d\n", grid->num_inlines);
      printf( "    Origin X,Y     %12.2f  %12.2f\n", grid->origin_x, grid->origin_y );
      printf( "    iline Delta   %12.2f  %12.2f\n", grid->inline_delta_x, grid->inline_delta_y );
      printf( "    Xline Delta    %12.2f  %12.2f\n", grid->xline_delta_x, grid->xline_delta_y );

      printf( "    iline First, increment   %8d %8d\n", grid->first_inline, grid->inline_increment);
      printf( "    Xline  First, increment   %8d %8d\n", grid->first_xline, grid->xline_increment);



      tricon_geometry_destroy( tg );
    }




}


