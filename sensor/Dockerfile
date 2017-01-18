from centos:7

RUN yum install -y epel-release && rpm -ivh http://repo.redborder.com/redborder-repo-0.0.3-1.el7.rb.noarch.rpm
RUN yum install -y redborder-monitor gettext-devel
RUN mkdir -p /opt/rb_monitor/conf
COPY config_env.json /opt/rb_monitor/conf
COPY sensor_start.sh /
CMD ./sensor_start.sh
