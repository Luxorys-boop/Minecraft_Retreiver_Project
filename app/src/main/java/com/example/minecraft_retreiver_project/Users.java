package com.example.minecraft_retreiver_project;

import java.util.List;

/**
 * Création de la classe Users : Elle contiendra toutes informations relative à un utilisateur.
 */
public class Users {

    private String email;
    private String mdp;
    private String pseudo;
    private List<Servers> servers;

    /**
     * Récupère l'email de l'utilisateur
     * @return l'email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Insère l'email de l'utilisateur
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Récupère le mot de passe de l'utilisateur.
     * @return le mot de passe.
     */
    public String getMdp() {
        return mdp;
    }

    /**
     * Insière le mot de passe de l'utilisateur.
     * @param mdp
     */
    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    /**
     * Récupère une liste des serveurs de l'utilisateur.
     * @return la liste serveurs
     */
    public List<Servers> getServers() {
        return servers;
    }

    /**
     * Insère une liste des serveurs de l'utilisateur.
     * @param servers
     */
    public void setServers(List<Servers> servers) {
        this.servers = servers;
    }

    /**
     * Récupère le pseudo de l'utilisateur.
     * @return le pseudo
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * Insère le pseudo de l'utilisateur.
     * @param pseudo
     */
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
}
