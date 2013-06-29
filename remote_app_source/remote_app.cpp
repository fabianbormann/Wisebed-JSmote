/*
 * remote_app.cpp
 *
 *  Created on: Jun 27, 2013
 *      Author: fabianbormann
 */

#include "external_interface/external_interface.h"
#include "algorithms/routing/tree/tree_routing.h"
#include "util/pstl/static_string.h"
#include "algorithms/protocols/coap/coap.h"

typedef wiselib::OSMODEL Os;
typedef wiselib::Coap<Os, Os::Radio, Os::Timer, Os::Debug, Os::Clock, Os::Rand, wiselib::StaticString> coap_t;

class RemoteApplication
{
   public:
      void init( Os::AppMainParameter& value ){
    	 ospointer = &value;
         radio_ = &wiselib::FacetProvider<Os, Os::Radio>::get_facet(value);
         timer_ = &wiselib::FacetProvider<Os, Os::Timer>::get_facet(value);
         debug_ = &wiselib::FacetProvider<Os, Os::Debug>::get_facet(value);
         uart_  = &wiselib::FacetProvider<Os, Os::Uart>::get_facet(value);
         clock_ = &wiselib::FacetProvider<Os, Os::Clock>::get_facet(value);

         // radio_->set_channel(12);

         debug_->debug( "Remote Application booting!\n" );

         radio_->enable_radio();
         debug_->debug( "Everything is fine -1-!\n" );
         rand_->srand(radio_->id());
         debug_->debug( "Everything is fine -2-!\n" );
         rand = (uint16_t) rand_->operator()(255);
         debug_->debug( "Everything is fine -3-!\n" );
         debug_->debug("Remote App %x", radio_->id());

         add_resources();

         coap_.init(*radio_, *timer_, *debug_, *clock_, rand, *uart_);

         radio_->reg_recv_callback<RemoteApplication,
                                   &RemoteApplication::receive_radio_message>( this );
         timer_->set_timer<RemoteApplication,
                          &RemoteApplication::broadcast_loop>( 5000, this, 0 );
      }

      void add_resources(){
          resource_t new_resource("alert", GET, true, 120, TEXT_PLAIN);
          new_resource.reg_callback<RemoteApplication, &RemoteApplication::alert > (this);
          coap_.add_resource(&new_resource);
      }

      void broadcast_loop(void*){
         debug_->debug( "broadcasting message at %x \n", radio_->id() );
         Os::Radio::block_data_t message[] = "hello world!\0";
         radio_->send( Os::Radio::BROADCAST_ADDRESS, sizeof(message), message );

         // following can be used for periodic messages to sink
         timer_->set_timer<RemoteApplication,
                           &RemoteApplication::broadcast_loop>( 5000, this, 0 );
      }

      void receive_radio_message(Os::Radio::node_id_t from, Os::Radio::size_t len, Os::Radio::block_data_t *buf){
         debug_->debug("received msg at %x from %x\n", radio_->id(), from);
         debug_->debug("message is %s\n", buf);
      }

      coap_status_t alert(callback_arg_t* args) {
              if (args->method == COAP_GET) {
            	  debug_->debug("hey there! :-) %s", args->input_data);
              }
              return INTERNAL_SERVER_ERROR;
          }
   private:
      Os::Radio::self_pointer_t radio_;
      Os::Timer::self_pointer_t timer_;
      Os::Debug::self_pointer_t debug_;
      Os::Clock::self_pointer_t clock_;
      Os::Rand::self_pointer_t rand_;
      Os::Uart::self_pointer_t uart_;
      coap_t coap_;
      uint16_t rand;
      Os::AppMainParameter* ospointer;
};

wiselib::WiselibApplication<Os, RemoteApplication> remote_app;

void application_main(Os::AppMainParameter& value){
  remote_app.init(value);
}
