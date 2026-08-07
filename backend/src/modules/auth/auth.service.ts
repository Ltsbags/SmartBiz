import { Injectable, UnauthorizedException, ConflictException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { PrismaService } from '../../prisma/prisma.service';
import { LoginDto } from './dto/login.dto';
import { RegisterBusinessDto } from './dto/register.dto';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import * as bcrypt from 'bcrypt';

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly jwtService: JwtService,
  ) {}

  async register(dto: RegisterBusinessDto) {
    const existingUser = await this.prisma.user.findUnique({
      where: { email: dto.email },
    });

    if (existingUser) {
      throw new ConflictException('User with this email already exists.');
    }

    const passwordHash = await bcrypt.hash(dto.password, 10);

    // Atomic transaction: Create Business, Main Branch, and Admin User
    return await this.prisma.$transaction(async (tx) => {
      const business = await tx.business.create({
        data: {
          name: dto.businessName,
          email: dto.email,
          phone: dto.phone,
        },
      });

      const branch = await tx.branch.create({
        data: {
          businessId: business.id,
          name: 'Main Branch',
          code: 'HQ-01',
          isHeadquarter: true,
          phone: dto.phone,
        },
      });

      const user = await tx.user.create({
        data: {
          businessId: business.id,
          branchId: branch.id,
          email: dto.email,
          fullName: dto.fullName,
          phone: dto.phone,
          passwordHash,
          role: 'ADMIN',
        },
      });

      const tokens = await this.generateTokens(user.id, business.id, branch.id, user.role);

      return {
        user: {
          id: user.id,
          email: user.email,
          fullName: user.fullName,
          role: user.role,
          businessId: business.id,
          branchId: branch.id,
        },
        ...tokens,
      };
    });
  }

  async login(dto: LoginDto) {
    const user = await this.prisma.user.findUnique({
      where: { email: dto.email },
    });

    if (!user || user.deletedAt) {
      throw new UnauthorizedException('Invalid credentials.');
    }

    const isMatch = await bcrypt.compare(dto.password, user.passwordHash);
    if (!isMatch) {
      throw new UnauthorizedException('Invalid credentials.');
    }

    const tokens = await this.generateTokens(user.id, user.businessId, user.branchId, user.role);

    return {
      user: {
        id: user.id,
        email: user.email,
        fullName: user.fullName,
        role: user.role,
        businessId: user.businessId,
        branchId: user.branchId,
      },
      ...tokens,
    };
  }

  async refreshToken(dto: RefreshTokenDto) {
    const storedToken = await this.prisma.refreshToken.findUnique({
      where: { token: dto.refreshToken },
      include: { user: true },
    });

    if (!storedToken || storedToken.isRevoked || new Date() > storedToken.expiresAt) {
      throw new UnauthorizedException('Invalid or expired refresh token.');
    }

    // Revoke old token (Refresh Token Rotation)
    await this.prisma.refreshToken.update({
      where: { id: storedToken.id },
      data: { isRevoked: true },
    });

    const tokens = await this.generateTokens(
      storedToken.userId,
      storedToken.user.businessId,
      storedToken.user.branchId,
      storedToken.user.role,
    );

    return tokens;
  }

  private async generateTokens(userId: string, businessId: string, branchId: string | null, role: string) {
    const payload = { sub: userId, businessId, branchId, role };

    const accessToken = this.jwtService.sign(payload, {
      expiresIn: '15m',
    });

    const refreshToken = this.jwtService.sign(payload, {
      expiresIn: '7d',
      secret: process.env.REFRESH_TOKEN_SECRET || 'super_secret_enterprise_refresh_token_signing_key_smartbiz_2026',
    });

    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 7);

    await this.prisma.refreshToken.create({
      data: {
        userId,
        token: refreshToken,
        expiresAt,
      },
    });

    return {
      accessToken,
      refreshToken,
      tokenType: 'Bearer',
      expiresInSeconds: 900,
    };
  }
}
