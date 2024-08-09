package cn.octautumn.csuemailclient.config;

import lombok.Data;

@Data
public class Config
{
    public String user_account = "";
    public String user_password = "";
    public String mail_pop_server_address = "pop.csu.edu.cn";
    public String mail_pop_server_port = "110";
    public String mail_smtp_server_address = "smtp.csu.edu.cn";
    public String mail_smtp_server_port = "25";
}
