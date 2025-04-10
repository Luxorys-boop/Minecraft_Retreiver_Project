package com.example.minecraft_retreiver_project;

/**
 * Création de la classe Servers : Elle contiendra toutes informations relative à un serveur.
 */
public class Servers {
    private String nom;
    private String ip;
    private String motd;

    /**
     * Récupère le nom du serveur.
     * @return le nom du serveur
     */
    public String getNom() {
        return nom;
    }

    /**
     * Change ou insère le nom d'un serveur
     * @param nom le nom du serveur
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Récupère l'adresse ip du serveur.
     * @return l'adresse ip du serveur.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Insère l'adresse ip du serveur.
     * @param ip l'adresse ip du serveur.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Récupère la description mise par le serveur.
     * @return la description.
     */
    public String getMotd() {
        return motd;
    }

    /**
     * Insère la description mise par le serveur.
     * @return la description.
     */
    public void setMotd(String motd) {
        this.motd = motd;
    }
}
