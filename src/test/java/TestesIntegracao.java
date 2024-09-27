import br.com.interfaces.model.IMusica;
import br.com.interfaces.model.IPlaylist;
import br.com.interfaces.model.IUsuario;
import br.com.interfaces.repository.IMusicaRepository;
import br.com.interfaces.repository.IUsuarioRepository;
import br.com.model.Usuario;
import br.com.repositories.MusicaRepository;
import br.com.repositories.UsuarioRepository;
import br.com.services.PlayListService;

import org.ufes.gqs.recomendacaoservice.services.RecomendacaoService;
import br.com.musicas.reproducao.ReproducaoService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

public class TestesIntegracao {
    private PlayListService playListService;
    private IUsuarioRepository usuarioRepository;
    private IUsuario usuario;
    private Optional<IPlaylist> pl;
    
    private RecomendacaoService recomendacaoService = new RecomendacaoService();
    private ReproducaoService reproducaoService = new ReproducaoService();
    
    @BeforeEach
    public void setUp() throws Exception{
        playListService = new PlayListService(recomendacaoService);
        usuario = new Usuario("Jao", "jao@email", Boolean.TRUE, Boolean.TRUE);
        
        usuarioRepository = UsuarioRepository.getUsuarioRepository();
        usuarioRepository.inserir(usuario);
        
        // Definindo uma playlist valida
        pl = playListService.criarPlayList("NaoSei", usuario);
        var musicas = MusicaRepository.getMusicaRepository().getMusicas("BlackPink");
        for(IMusica music : musicas.get() ){
            pl.get().adicionarMusica(music);
        }
    }
    
    @Test
    // Retorna uma lista de músicas recomendadas para adicionar à playlist.
    public void recomendarMusicasParaPlayListTeste(){
        IMusicaRepository musicaRepository = MusicaRepository.getMusicaRepository();
        List<IMusica> resultado = playListService.recomendarMusicasParaPlayList(pl.get());
        
        assertEquals(Boolean.FALSE , resultado.isEmpty());
        assertTrue(resultado.contains(musicaRepository.findByTitulo("What is Love").get()));
        assertTrue(resultado.contains(musicaRepository.findByTitulo("Gangnam Style").get()));
    }
    
    @Test
    // Retorna uma lista vazia se não houver músicas disponíveis para recomendação.
    public void recomendarMusicasParaPlayListTesteFalha1(){
        // Definindo uma playlist com todas as musicas
        var playList = playListService.criarPlayList("TesteFullMusicas", usuario);
        
        var musicas = MusicaRepository.getMusicaRepository().getAll();
        for(IMusica music : musicas ){
            playList.get().adicionarMusica(music);
        }
        
        List<IMusica> resultado = playListService.recomendarMusicasParaPlayList(playList.get());
        
        // Esta dando errado pq mesmo adicionando todas as musicas ele esta retornando 10 musicas
        assertEquals(Boolean.TRUE , resultado.isEmpty());
    }
    
    @Test
    // Retorna uma lista vazia se o RecomendacaoService não estiver disponível.
    public void recomendarMusicasParaPlayListTesteFalha2() {
        // Forcando a lancar uma exception com recomedacao service igual a null
        PlayListService service = new PlayListService(null);
        
        List<IMusica> resultado = service.recomendarMusicasParaPlayList(pl.get());

        // Verificando se o resultado é uma lista vazia, já que o serviço está indisponível (retornou null)
        assertTrue(resultado.isEmpty());
    }

    @Test
    // Inicia a reprodução da playlist automaticamente.
    public void iniciarReproducaoPlayList(){
        var retorno = playListService.iniciarReproducaoPlayList(pl.get(), usuario, reproducaoService);
        
        assertEquals(0, retorno);
    }
    
    @Test
    public void iniciarReproducaoPlayListFalha1(){
        IUsuario us = new Usuario("Meu nome", "email@email", Boolean.FALSE, Boolean.TRUE);
        
        var playListVazia = playListService.criarPlayList("Vazia", us);
        
        var retorno = playListService.iniciarReproducaoPlayList(playListVazia.get(), us, reproducaoService);
        
        assertEquals(-1, retorno);
    }
    
    @Test
    public void iniciarReproducaoPlayListFalha2(){
        var playListVazia = playListService.criarPlayList("Vazia", usuario);
        
        var retorno = playListService.iniciarReproducaoPlayList(playListVazia.get(), usuario, reproducaoService);
        
        assertEquals(-2, retorno);
    }
}
